package com.csproj.Cyberlab.API.user.labInstance;

import com.csproj.Cyberlab.API.course.Course;
import com.csproj.Cyberlab.API.course.CourseService;
import com.csproj.Cyberlab.API.exceptions.ForbiddenException;
import com.csproj.Cyberlab.API.exceptions.InternalServerException;
import com.csproj.Cyberlab.API.exceptions.NotFoundException;
import com.csproj.Cyberlab.API.labTemplate.LabTemplate;
import com.csproj.Cyberlab.API.labTemplate.LabTemplateRepo;
import com.csproj.Cyberlab.API.user.User;
import com.csproj.Cyberlab.API.user.labInstance.vmInstance.VmInstance;
import com.csproj.Cyberlab.API.user.labInstance.vmInstance.VmInstanceService;
import com.csproj.Cyberlab.API.user.labInstance.vmInstance.VncConnectionResponse;
import com.csproj.Cyberlab.API.virtualization.VirtualizationProvider;
import com.csproj.Cyberlab.API.virtualization.proxmox.ProxmoxClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.*;

//---------------------------------------------------------------
// Manage all LabInstance related business logic
//
// LabInstance exists as a nested document inside User with no real
// distinction on the persistence layer. On the API we pretend they
// are separate, by extracting / inserting LabInstance from the User
// object in our queries
//
//---------------------------------------------------------------
@Service
@RequiredArgsConstructor
public class LabInstanceService {

    private final LabTemplateRepo labTemplateRepo; // Exception to service imports - need to avoid dependency cycle
    private final VmInstanceService vmInstanceService;
    private final CourseService courseService;
    private final VirtualizationProvider virtualizationProvider;

    //---------------------------------------------------------------------------
    // LabInstance Business Logic
    //---------------------------------------------------------------------------

    /**
     * Query database for a LabInstance by its ID
     *
     * @param user owner of the LabInstance
     * @param labId ID of the LabInstance to get
     * @return LabInstance found
     * @throws NotFoundException no LabInstance associated with requested IDs
     */
    public LabInstance findById(User user, String labId) throws NotFoundException {
        return user.getLabInstances().values().stream()
                .filter(lab -> lab.getId().equals(labId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Requested LabInstance not found", null));
    }

    /**
     * Updates a LabInstance object, does not save to persistence layer
     *
     * @param user User owning the LabInstance
     * @param labId ID of the LabInstance to update
     * @param dto POJO containing data contents to update
     * @return Updated LabInstance
     * @throws NotFoundException no LabInstance associated with provided ID
     * @throws ForbiddenException LabInstance is already marked as completed
     */
    public LabInstance update(User user, String labId, LabInstanceUpdateDTO dto) {
        LabInstance lab = findById(user, labId);

        if (lab.isCompleted()) {
            throw new ForbiddenException("LabInstance is already completed and cannot be modified", null);
        }

        // Apply update
        lab.setUserAnswers(dto.userAnswers());
        lab.setCompleted(true);
        lab.setDateLastAccessed(new Date(System.currentTimeMillis()));

        return lab;
    }

    /**
     * Deletes the specified VmInstance and removes it from the specified LabInstance
     *
     * @param user User owning the LabInstance and VmInstance
     * @param labId ID of the LabInstance containing the VmInstance to delete
     * @param vmId ID of the VmInstance to delete
     * @return Updated LabInstance excluding the deleted VmInstance
     */
    public LabInstance removeVmInstance(User user, String labId, String vmId) {
        LabInstance lab = findById(user, labId);
        VmInstance deletedVm = vmInstanceService.delete(lab, vmId);
        Map<String, VmInstance> vms = lab.getVmInstances();

        vms.entrySet().removeIf(vm -> vm.getValue().equals(deletedVm));
        lab.setVmInstances(vms);

        return lab;
    }

    /**
     * Creates a new LabInstance and all associated VmInstance
     * Does not save to persistence layer
     *
     * @param templateId ID of the LabTemplate to create the LabInstance from
     * @param courseId ID of the course associated with the LabInstance
     * @param dueDate Date the LabInstance is due
     * @return new LabInstance
     */
    public LabInstance create(String templateId, String courseId, Date dueDate) {
        LabTemplate labTemplate = labTemplateRepo.findById(templateId)
                .orElseThrow(() -> new NotFoundException("LabTemplate not found", null));

        Course course = courseService.findById(courseId);

        Map<String, VmInstance> vmInstances = createVmInstances(labTemplate.getVmTemplateIds());

        this.virtualizationProvider.addLabInstanceNetworking(vmInstances);

        Map<String, LabInstanceQuestion> questions = new HashMap<>();
        for (String key : labTemplate.getQuestions().keySet()) {
            questions.put(key, new LabInstanceQuestion(labTemplate.getQuestions().get(key)));
        }

        return new LabInstance(
                labTemplate.getName(),
                labTemplate.getDescription(),
                labTemplate.getObjectives(),
                questions,
                course.getId(),
                labTemplate.getId(),
                new Date(System.currentTimeMillis()),
                vmInstances,
                new ArrayList<>(),
                false,
                dueDate
        );
    }

    //---------------------------------------------------------------------------
    // VmInstance Business Logic
    //---------------------------------------------------------------------------

    /**
     * Creates the VmInstances from a provided list of template ids
     *
     * @param templateIds IDs of templates to create VmInstance from
     * @return List of new VmInstance
     */
    private Map<String, VmInstance> createVmInstances(List<String> templateIds) {
        Map<String, VmInstance> instances = new HashMap<>();
        templateIds.forEach((id) -> {
            VmInstance vm = vmInstanceService.create(id);
            instances.put(vm.getId(), vm);
        });

        return instances;
    }

    /**
     * Wrapper function for GET LabInstanceById
     * Serves as proxy for VmInstanceService request to avoid circular dependency
     *
     * @param user User owning the VmInstance
     * @param labId LabInstance containing the VmInstance
     * @param vmId ID of the requested VmInstance
     * @return found VmInstance
     */
    public VmInstance findVmInstanceById(User user, String labId, String vmId) {
        LabInstance lab = findById(user, labId);

        return vmInstanceService.findById(lab, vmId);
    }

    /**
     * Wrapper function for GET VncConnString
     * Serves as proxy for VmInstanceService request to avoid circular dependency
     *
     * @param user User owning the VmInstance
     * @param labId LabInstance containing the VmInstance
     * @param vmId ID of the requested VmInstance
     * @return found VmInstance
     */
    public VncConnectionResponse getVmInstanceConnString(User user, String labId, String vmId) {
        LabInstance lab = findById(user, labId);

        return vmInstanceService.getConnString(lab, vmId);
    }

}
