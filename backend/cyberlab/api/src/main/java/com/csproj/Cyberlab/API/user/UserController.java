package com.csproj.Cyberlab.API.user;

import com.csproj.Cyberlab.API.user.labInstance.*;
import com.csproj.Cyberlab.API.user.labInstance.vmInstance.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

//---------------------------------------------------------------------------
// Provide mappings for clients to interact with Users and nested objects
//---------------------------------------------------------------------------
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = """
        Users store all related data via directly nested documents.
        Operations to request nested objects directly are supported however they are very slow.
        The authenticated User is returned upon successful login, it is recommended to cache this whenever possible.
        
        **Resource Level Authorization:**
        - If a client has access to a User, they also have access to all nested data (PID excluded).
        """)
@Slf4j
public class UserController {
    private final UserService userService;

    //---------------------------------------------------------------------------
    // User Mappings
    //---------------------------------------------------------------------------

    /**
     * Get a User by ID
     *
     * @param userId ID of the requested User
     * @return User associated with request ID
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get a User by ID",
            description = """
                    **Authorized User Types:**
                    - Admin
                    - Instructor
                    - Student
                    """)
    @PreAuthorize("(hasRole('ROLE_ADMIN')) or (hasRole('ROLE_INSTRUCTOR')) or (hasRole('ROLE_STUDENT'))")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable String userId) {
        log.info("Received request to GET User by ID");
        log.trace("Received request to GET User: users/" + userId);

        User user = userService.findById(userId);
        UserResponseDTO res = new UserResponseDTO(user);

        log.trace(String.format("Fulfilled request to get User with (user = %s)", res));

        return ResponseEntity.ok().body(res);
    }

    /**
     * Creates a new user
     *
     * @param dto New user data
     * @param ucb UriComponentsBuilder
     * @return New user data
     */
    @PostMapping
    @Operation(summary = "Create a new User",
            description = """
                    **Authorized User Types:**
                    - Admin
                    
                    **Note:**
                    - User registration is not currently supported. Contact if this is a necessary feature.
                    """)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserResponseDTO> create(@RequestBody UserCreateDTO dto, UriComponentsBuilder ucb) {
        log.info("Received request to CREATE User");
        log.trace("Recieved request to CREATE User: " + dto);

        User user = userService.create(dto);
        UserResponseDTO res = new UserResponseDTO(user);

        URI uri = ucb
                .path("/{id}")
                .buildAndExpand(user.getId())
                .toUri();

        log.trace(String.format("Fulfilled request to get User with (user = %s)", res));

        return ResponseEntity.created(uri).body(res);
    }

    //---------------------------------------------------------------------------
    // LabInstance Mappings
    //---------------------------------------------------------------------------

    /**
     * Get a LabInstance by id
     *
     * @param userId ID of User owning the labInstance
     * @param labId ID of requested LabInstance
     * @return LabInstance associated with requested ID
     */
    @GetMapping("/{userId}/labInstances/{labId}")
    @Operation(summary = "Retrieve a User's LabInstance by ID",
            description = """
                    **Authorized User Types:**
                    - Admin
                    - Instructor
                    - Student
                    """)
    @PreAuthorize("(hasRole('ROLE_ADMIN')) or (hasRole('ROLE_INSTRUCTOR')) or (hasRole('ROLE_STUDENT'))")
    public ResponseEntity<LabInstanceResponseDTO> findById(@PathVariable String userId, @PathVariable String labId) {
        log.info("Received request to get LabInstance by ID");
        log.trace("Received request GET LabInstance: users/" + userId + "/labInstances/" + labId);

        LabInstance labInstance = userService.findLabInstanceById(userId, labId);
        LabInstanceResponseDTO res = new LabInstanceResponseDTO(labInstance);

        log.trace(String.format("Fulfilled request to get LabInstance with (labInstance = %s)", res));

        return ResponseEntity.ok().body(res);
    }

    /**
     * Create a new labInstance
     *
     * @param userId ID of User owning the labInstance
     * @param dto new labInstance data
     * @param ucb auto-injected builder for new URI object
     * @return LabInstance created
     */
    @PostMapping("{userId}/labInstances")
    @Operation(summary = "Creates a new LabInstance",
            description = """
                    **Authorized User Types:**
                    - Admin
                    - Instructor
                    
                    **Note:**
                    - Only one LabInstance can be created per LabTemplate per user.
                    """)
    @PreAuthorize("(hasRole('ROLE_ADMIN')) or (hasRole('ROLE_INSTRUCTOR'))")
    public ResponseEntity<LabInstanceResponseDTO> create(@PathVariable String userId, @RequestBody LabInstanceCreateDTO dto, UriComponentsBuilder ucb) {
        log.info("Received request to create LabInstance");
        log.trace("Received request to create LabInstance: /users/" + userId + "/labInstances with: " + dto);

        LabInstance newLabInstance = userService.createLabInstance(userId, dto);
        LabInstanceResponseDTO res = new LabInstanceResponseDTO(newLabInstance);
        URI uri = ucb
                .path("lab-instance/{id}")
                .buildAndExpand(newLabInstance.getId())
                .toUri();

        return ResponseEntity.created(uri).body(res);
    }

    /**
     * Updates an existing LabInstance unless missing or already complete
     *
     * @param userId ID of the User owning the LabInstance
     * @param labId ID of the LabInstance to update
     * @param dto POJO containing data to update with
     * @return Updated LabInstance
     */
    @PatchMapping("/{userId}/labInstances/{labId}")
    @Operation(summary = "Update Lab Instance",
            description = """
                    **Authorized User Types**
                    - Admin
                    - Instructor
                    - Student
                    
                    **Note:**
                    - Submits a User's lab answers and marks the lab as complete.
                    - Once completed, subsequent completion requests are refused.
                    """)
    public ResponseEntity<LabInstanceResponseDTO> updateLabInstance(@PathVariable String userId, @PathVariable String labId, @RequestBody LabInstanceUpdateDTO dto) {
        log.info("Received request to update LabInstance");
        log.trace("Received request to update LabInstance: /users/" + userId + "/labInstances/" + labId + " with: " + dto);

        LabInstance lab = userService.updateLabInstance(userId, labId, dto);
        LabInstanceResponseDTO res = new LabInstanceResponseDTO(lab);

        log.info("LabInstance with ID {} successfully updated", labId);

        return ResponseEntity.ok(res);
    }

    //---------------------------------------------------------------------------
    // VmInstance Mappings
    //---------------------------------------------------------------------------

    /**
     * Get a VmInstance by ID
     *
     * @param userId ID of the containing User
     * @param labId ID of the containing LabInstance
     * @param vmId ID of the requested VmInstance
     * @return Found VmInstance
     */
    @GetMapping("/{userId}/labInstances/{labId}/vmInstances/{vmId}")
    @Operation(summary = "Get VM Instance by ID",
            description = """
                **Authorized User Types:**
                - Admin
                - Instructor
                - Student
                """)
    public ResponseEntity<VmInstanceResponseDTO> findById(@PathVariable String userId, @PathVariable String labId, @PathVariable String vmId) {
        log.info("Received request to get VmInstance by ID");
        log.trace("Received GET request for VmInstance: /users/" + userId + "/labInstances/" + labId + "/vmInstances/" + vmId);

        VmInstance vmInstance = userService.findVmInstanceById(userId, labId, vmId);
        VmInstanceResponseDTO res = new VmInstanceResponseDTO(vmInstance);

        log.trace(String.format("Fulfilled request to get VmInstance with (vmInstance = %s)", res));

        return ResponseEntity.ok().body(res);
    }

    /**
     * Delete a VmInstance
     *
     * @param userId ID of the User who owns the VmInstance
     * @param labId ID of the LabInstance containing the VmInstance to delete
     * @param vmId ID of the VmInstance to Delete
     */
    @DeleteMapping("/{userId}/labInstances/{labId}/vmInstances/{vmId}")
    @Operation(summary = "Deletes a VM Instance from Proxmox and MongoDB",
            description = """
                **Authorized User Types:**
                - Admin
                
                **Note:**
                - VmInstances are generally cleaned up by a garbage collection routine depending on the due date of their parent lab.
                """)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteVmInstance(@PathVariable String userId, @PathVariable String labId, @PathVariable String vmId) {
        log.info("Received request to delete VmInstance");
        log.trace("Received DELETE request for VmInstance: /users/" + userId + "/labInstances/" + labId + "/vmInstances/" + vmId);

        userService.deleteVmInstance(userId, labId, vmId);

        log.trace(String.format("Successfully deleted VmInstance with (id = %s)", vmId));
        return ResponseEntity.noContent().build();
    }

    /**
     * Get VNC ticket and port number.
     *
     * @param userId ID of the containing User
     * @param labId ID of the containing LabInstance
     * @param vmId ID of the VmInstance to connect to
     * @return Vm connection credentials
     */
    @GetMapping("/{userId}/labInstances/{labId}/vmInstances/{vmId}/vnc")
    @Operation(summary = "Retrieve VNC credentials for a VM console",
            description = """
                    **Authorized User Types:**
                    - Admin
                    - Instructor
                    - Student
                    """
    )
    public ResponseEntity<VncConnectionResponse> getConnString(@PathVariable String userId, @PathVariable String labId, @PathVariable String vmId) {
        log.info("Received VNC connection request");
        log.trace("Received VNC connection request: /users/" + userId + "/labInstances/" + labId + "/vmInstances/" + vmId);

        VncConnectionResponse vncConnection = userService.getVmInstanceConnString(userId, labId, vmId);

        log.trace(String.format("Fulfilled VNC connection request with VNC connection to (vm = %s, port = %s)", vmId, vncConnection.getVncPort()));

        return ResponseEntity.ok().body(vncConnection);
    }
}
