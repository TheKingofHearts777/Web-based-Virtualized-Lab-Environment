package com.csproj.Cyberlab.API.vmTemplate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

//---------------------------------------------------------------
// Provide mappings for clients to interact with VmTemplates
//---------------------------------------------------------------
@RestController
@RequestMapping("/vm-template")
@RequiredArgsConstructor
@Tag(name = "VmTemplates", description = """
        VmTemplates encapsulate preconfigured VM VDI drives with OS, software & configurations.
        VmTemplates are cloned into VmInstances to create the VMs Users interact with during Labs.
        """)
@Slf4j
public class VmTemplateController {
    private final VmTemplateService vmTemplateService;

    @PostMapping(value = "/upload", consumes = { "application/x-virtualbox-vdi" })
    @Operation(summary = "Upload a VM Template via a preconfigured VDI file",
            description = """
                    **Authorized User Types:**
                    - Admin
                    - Instructor
                    
                    **Max VDI Size:**
                    - 10GB
                    """)
    @PreAuthorize("(hasRole('ROLE_INSTRUCTOR')) or (hasRole('ROLE_ADMIN'))")
    public ResponseEntity<VmTemplate> uploadFile(HttpServletRequest request,
                                             @RequestHeader("name") String name,
                                             @RequestHeader("description") String description,
                                             UriComponentsBuilder ucb) throws Exception
    {
        log.trace("Received request to upload VmTemplate");
        log.trace("Received request to upload VmTemplate with parameters: (name: " + name + ", description: " + description + ")");

        InputStream inputStream = request.getInputStream();

        VmTemplate newVmTemplate = vmTemplateService.uploadVdi(inputStream, name, description);
        URI uri = ucb
                .path("vm-template/{id}")
                .buildAndExpand(newVmTemplate)
                .toUri();

        log.trace(String.format("Fulfilled request to upload VmTemplate with (vmTemplate = %s)", newVmTemplate));

        return ResponseEntity.created(uri).body(newVmTemplate);
    }

    /**
     * Get list of VM templates existing in database.
     *
     * @return paginated list of VM templates
     */
    @GetMapping("/list")
    @Operation(summary = "Retrieves a paginated list from all VM Templates",
            description = """
                    **Authorized User Types:**
                    - Admin
                    - Instructor
                    """)
    @PreAuthorize("(hasRole('ROLE_INSTRUCTOR')) or (hasRole('ROLE_ADMIN'))")
    public ResponseEntity<List<VmTemplateResponseDTO>> getVmTemplateList(@RequestParam int limit, @RequestParam int offset) {
        log.info("Received request for VmTemplate list");

        List<VmTemplate> vmTemplateList = vmTemplateService.getVmTemplateList(limit, offset);
        List<VmTemplateResponseDTO> res = new ArrayList<>(
                vmTemplateList.stream().map(VmTemplateResponseDTO::new).toList()
        );

        log.trace(String.format("Fulfilled VmTemplate list request with (listSize = %s)", vmTemplateList.size()));

        return ResponseEntity.ok().body(res);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a VM Template",
            description = """
                    **Authorized User Types:**
                    - Admin
                    - Instructor
                    """)
    @PreAuthorize("(hasRole('ROLE_INSTRUCTOR')) OR (hasRole('ROLE_ADMIN'))")
    public ResponseEntity<Void> deleteVmTemplate(@PathVariable String id) {
        log.trace(String.format("Fulfilled request to delete VmTemplate with (id = %s)", id));

        vmTemplateService.delete(id);

        log.trace(String.format("Successfully deleted VmTemplate with (id = %s)", id));
        return ResponseEntity.noContent().build();
    }
}
