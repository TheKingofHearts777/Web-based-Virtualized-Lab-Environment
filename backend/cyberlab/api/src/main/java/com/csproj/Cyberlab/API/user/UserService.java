package com.csproj.Cyberlab.API.user;

import com.csproj.Cyberlab.API.auth.AuthService;
import com.csproj.Cyberlab.API.exceptions.BadRequestException;
import com.csproj.Cyberlab.API.exceptions.NotFoundException;
import com.csproj.Cyberlab.API.user.labInstance.LabInstance;
import com.csproj.Cyberlab.API.user.labInstance.LabInstanceCreateDTO;
import com.csproj.Cyberlab.API.user.labInstance.LabInstanceService;
import com.csproj.Cyberlab.API.user.labInstance.LabInstanceUpdateDTO;
import com.csproj.Cyberlab.API.user.labInstance.vmInstance.VmInstance;
import com.csproj.Cyberlab.API.user.labInstance.vmInstance.VmInstanceService;
import com.csproj.Cyberlab.API.user.labInstance.vmInstance.VncConnectionResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

//---------------------------------------------------------------
// Manage all User related business logic
//---------------------------------------------------------------
@Service
@Slf4j
@AllArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    private final LabInstanceService labInstanceService;
    private final VmInstanceService vmInstanceService;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    //---------------------------------------------------------------------------
    // User Business Logic
    //---------------------------------------------------------------------------

    /**
     * Query database for a User by its id
     *
     * @param id ID of requested User
     * @return User found
     * @throws NotFoundException no User associated with supplied ID
     */
    public User findById(String id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Requested User not found", null));

        authService.authorizeResourceAccess(user.getId());

        return user;
    }

    /**
     * Creates a new User and saves to persistence layer
     * Nested User resources are initialized to null.
     *
     * @param dto New User data
     * @return Newly created user
     * @throws BadRequestException Username already taken
     */
    public User create(UserCreateDTO dto) {
        if (userRepo.findByUsername(dto.getUsername()).isPresent()) {
            Map<String, String> ctx = new HashMap<>() {{
                put("username:", dto.getUsername());
            }};
            throw new BadRequestException("Username already taken", ctx);
        }

        User newUser = new User(
                dto.getUsername(),
                passwordEncoder.encode(dto.getPassword()),
                dto.getUserType()
        );

        return userRepo.save(newUser);
    }

    /**
     * Deletes provided User by its id
     *
     * @param id ID of User to delete
     * @throws NotFoundException User to delete doesn't exist
     */
    public void deleteUser(String id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Requested User not found", null));

        userRepo.deleteById(user.getId());
    }

    //---------------------------------------------------------------------------
    // LabInstance Business Logic
    //---------------------------------------------------------------------------

    /**
     * Wrapper function for GET LabInstanceById
     * Serves as proxy for LabInstanceService request to avoid circular dependency
     *
     * @param userId ID of User owning the LabInstance
     * @param labId ID of Lab to GET
     * @return found LabInstance
     */
    public LabInstance findLabInstanceById(String userId, String labId) {
        User user = findById(userId);

        return labInstanceService.findById(user, labId);
    }

    /**
     * Get a list of LabInstance that are expirationThreshold past their due date
     *
     * @param expirationThreshold Cutoff time for expired LabInstance
     * @return List of expired LabInstance and their parent User ID
     */
    public List<ExpirationRecord> getExpiredLabInstances(Date expirationThreshold) {
        List<ExpirationRecord> exp = new ArrayList<>();

        for (User user : userRepo.findByExpiredLabInstance(expirationThreshold)) {
            // Filter out expired lab instances
            List<LabInstance> expiredLabs = user.getLabInstances().values().stream()
                    .filter(labInstance -> labInstance.getDueDate().after(expirationThreshold))
                    .collect(Collectors.toList());

            // Only add a record if the user has one or more expired labs
            if (!expiredLabs.isEmpty()) {
                exp.add(new ExpirationRecord(user.getId(), expiredLabs));
            }
        }

        return exp;
    }

    /**
     * Adds a new LabInstance to a User
     *
     * @param userId ID of the User to create the LabInstance for
     * @param dto Original request body with template and course ids
     * @return New LabInstance
     */
    public LabInstance createLabInstance(String userId, LabInstanceCreateDTO dto) {
        User user = findById(userId);
        boolean userHasLab = user.getLabInstances().values().stream()
                .anyMatch(instance -> instance.getTemplateId().equals(dto.getTemplateId()));

        if (userHasLab) {
            Map<String, String> ctx = new HashMap<>();
            ctx.put("UserId", user.getId());
            ctx.put("LabTemplateId", dto.getTemplateId());

            throw new BadRequestException("User already has a LabInstance for the supplied Template", ctx);
        }

        LabInstance lab = labInstanceService.create(dto.getTemplateId(), dto.getCourseId(), dto.getDueDate());
        addLabInstance(user, lab);

        return lab;
    }

    /**
     * Update LabInstance overload for when User and LabInstance are not yet retrieved
     *
     * @param userId ID of the User owning the LabInstance
     * @param labId ID of the LabInstance to update
     * @param dto POJO containing data to update with
     * @return Updated LabInstance
     */
    public LabInstance updateLabInstance(String userId, String labId, LabInstanceUpdateDTO dto) {
        User user = findById(userId);
        LabInstance updatedLab = labInstanceService.update(user, labId, dto);

        return updateLabInstance(user, updatedLab);
    }

    /**
     * Update a User's LabInstance with a new one of the same ID
     * Replaces the LabInstance in the persistence layer
     *
     * @param user User owning the LabInstance
     * @param updatedLab Lab to replace the existing one with - must have the same ID
     * @return Updated LabInstance
     */
    private LabInstance updateLabInstance(User user, LabInstance updatedLab) {
        Map<String, LabInstance> labs = user.getLabInstances();

        // Compares on LabInstance ID
        labs.entrySet().removeIf(lab -> lab.getValue().equals(updatedLab));
        addLabInstance(user, updatedLab);

        return updatedLab;
    }

    /**
     * Appends a LabInstance to an existing Users LabInstances
     * Saves to persistence layer
     *
     * @param user user to append to
     * @param newLab pre-created & validated LabInstance to append
     */
    private void addLabInstance(User user, LabInstance newLab) {
        Map<String, LabInstance> labs = user.getLabInstances();
        labs.put(newLab.getId(), newLab);
        user.setLabInstances(labs);
        userRepo.save(user);
    }

    /**
     * Deletes a LabInstance from the database.
     *
     * @param user User containing the LabInstance.
     * @param lab  LabInstance to delete.
     */
    public void deleteLabInstance(User user, LabInstance lab) {
        if (user == null || lab == null) {
            return;
        }

        String labId = lab.getId();

        if (!user.getLabInstances().containsKey(labId)) {
            log.warn(String.format("LabInstance %s not found for User %s. Skipping deletion.", labId, user.getId()));
            return;
        }

        log.info(String.format("Deleting LabInstance %s for User %s...", labId, user.getId()));

        // Delete all associated VM Instances before deleting the LabInstance
        for (String vmId : new ArrayList<>(lab.getVmInstances().keySet())) {
            try {
                vmInstanceService.delete(lab, vmId);
                log.info(String.format("Deleted VM Instance %s from LabInstance %s.", vmId, labId));
            } catch (Exception e) {
                log.error(String.format("Failed to delete VM Instance %s from LabInstance %s.", vmId, labId), e);
            }
        }

        // Remove the LabInstance from the User's records
        user.getLabInstances().remove(labId);
        log.info(String.format("LabInstance %s removed from User %s.", labId, user.getId()));

        // Update the user in the database
        userRepo.save(user);
        log.info(String.format("User %s updated in database after removing LabInstance %s.", user.getId(), labId));

        log.info(String.format("Successfully deleted LabInstance %s for User %s.", labId, user.getId()));
    }


    //---------------------------------------------------------------------------
    // VmInstance Business Logic
    //---------------------------------------------------------------------------

    /**
     * Wrapper function for GET VmInstanceById
     * Serves as proxy for LabInstanceService request to avoid circular dependency
     *
     * @param userId ID of User owning the VmInstance
     * @param labId ID of LabInstance the VmInstance is contained in
     * @param vmId ID of the requested VmInstance
     * @return found VmInstance
     */
    public VmInstance findVmInstanceById(String userId, String labId, String vmId) {
        User user = findById(userId);

        return labInstanceService.findVmInstanceById(user, labId, vmId);
    }

    /**
     * Wrapper function for DELETE VmInstance
     * The VmInstance is deleted from upstream VirtualizationProvider and removed from the LabInstance
     * the updated LabInstance is replaced in User, then saved to the database.
     *
     * @param userId ID of the User owning the VmInstance
     * @param labId ID of the LabInstance containing the VmInstance to delete
     * @param vmId ID of the VmInstance to delete
     */
    public void deleteVmInstance(String userId, String labId, String vmId) {
        User user = findById(userId);
        LabInstance lab = labInstanceService.removeVmInstance(user, labId, vmId);
        updateLabInstance(user, lab);
    }

    /**
     * Retrieves a list VmInstance that have the specified parent id
     * This is an extremely slow operation since it essentially has to iterate all users
     * LabInstances, and VmInstances on both the db and api to find matchers
     *
     * @param parentId ID of the VmTemplate the VmInstance was cloned from
     * @return List of VmInstance with the specified parent ID
     */
    public List<VmInstance> findVmInstancesByParentId(String parentId) {
        return userRepo.findByVmInstanceParentId(parentId).stream()
                .flatMap(user -> user.getLabInstances().values().stream())
                .flatMap(lab -> lab.getVmInstances().values().stream())
                .filter(vm -> vm.getVmParentId().equals(parentId))
                .collect(Collectors.toList());
    }

    /**
     * Wrapper function for GET VmConnString
     * Serves as proxy for LabInstanceService request to avoid circular dependency
     *
     * @param userId ID of User owning the VmInstance
     * @param labId ID of LabInstance the VmInstance is contained in
     * @param vmId ID of the VmInstance to connect to
     * @return Connection credentials to the requested VmInstance
     */
    public VncConnectionResponse getVmInstanceConnString(String userId, String labId, String vmId) {
        User user = findById(userId);

        return labInstanceService.getVmInstanceConnString(user, labId, vmId);
    }
}
