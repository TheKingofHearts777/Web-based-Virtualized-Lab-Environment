package com.csproj.Cyberlab.API.labTemplate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

//---------------------------------------------------------------
// Provide mappings for clients to interact with LabTemplates
//---------------------------------------------------------------
@RestController
@RequestMapping("/lab-template")
@RequiredArgsConstructor
@Tag(name = "LabTemplates", description = """
        LabTemplates encapsulate all Lab related information including questions and VmTemplates.
        LabTemplates are used to model LabInstances which represent the Labs Users interact with.
        """)
@Slf4j
public class LabTemplateController {

    private final LabTemplateService labTemplateService;

    /**
     * Get a LabTemplate by id
     *
     * @param id ID of requested LabTemplate
     * @return LabTemplate associated with requested ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Retrieve a specific Lab Template by its unique ID",
            description = """
                    **Authorized User Types:**
                    - Admin
                    - Instructor
                    """)
    @PreAuthorize("(hasRole('ROLE_INSTRUCTOR')) or (hasRole('ROLE_ADMIN'))")
    public ResponseEntity<LabTemplateResponseDTO> findById(@PathVariable String id) {
        log.info("Received request to get LabTemplate by ID");
        log.trace(String.format("Received request for LabTemplate with parameters (id = %s)", id));

        LabTemplate labTemplate = labTemplateService.findById(id);
        LabTemplateResponseDTO res = new LabTemplateResponseDTO(labTemplate);

        log.trace(String.format("Fulfilled request to get LabTemplate with (labTemplate = %s)", labTemplate));

        return ResponseEntity.ok().body(res);
    }

    /**
     * Create a new LabTemplate
     *
     * @param labTemplate LabTemplate to create
     * @param ucb auto-injected builder for new URI object
     * @return LabTemplate created
     */
    @PostMapping
    @Operation(summary = "Create a new Lab Template",
            description = """
                    **Authorized User Types:**
                    - Admin
                    - Instructor
                    """)
    @PreAuthorize("(hasRole('ROLE_INSTRUCTOR')) or (hasRole('ROLE_ADMIN'))")
    public ResponseEntity<LabTemplateResponseDTO> create(@RequestBody LabTemplate labTemplate, UriComponentsBuilder ucb) {
        log.info("Received request to create LabTemplate");
        log.trace(String.format("Received request to create LabTemplate with (labTemplate = %s)", labTemplate));

        LabTemplate newLabTemplate = labTemplateService.create(labTemplate);
        LabTemplateResponseDTO res = new LabTemplateResponseDTO(newLabTemplate);
        URI uri = ucb
                .path("lab-template/{id}")
                .buildAndExpand(newLabTemplate)
                .toUri();

        log.trace(String.format("Fulfilled request to create LabTemplate with (labTemplate = %s)", newLabTemplate));

        return ResponseEntity.created(uri).body(res);
    }

    /**
     * Get list of lab templates existing in database.
     *
     * @return paginated list of lab templates
     */
    @GetMapping("/list")
    @Operation(summary = "Retrieves a paginated list of all LabTemplates",
            description = """
                    **Authorized User Types:**
                    - Admin
                    - Instructor
                    """)
    @PreAuthorize("(hasRole('ROLE_INSTRUCTOR')) or (hasRole('ROLE_ADMIN'))")
    public ResponseEntity<List<LabTemplateResponseDTO>> getLabTemplateList(@RequestParam int limit, @RequestParam int offset) {
        log.info("Received request for LabTemplate list");

        List<LabTemplate> labTemplateList = labTemplateService.getLabTemplateList(limit, offset);
        List<LabTemplateResponseDTO> res = new ArrayList<>(
                labTemplateList.stream().map(LabTemplateResponseDTO::new).toList()
        );

        log.trace(String.format("Fulfilled LabTemplate list request with (listSize = %s)", labTemplateList.size()));

        return ResponseEntity.ok().body(res);
    }

    /**
     * Deletes a LabTemplate and all associated VMs (Templates and Instances)
     *
     * @param id LabTemplate id to delete
     * @return void
     *
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Cascade deletes a LabTemplate and all associated VmTemplate and VmInstance",
            description = """
                    **Authorized User Types:**
                    - Admin
                    - Instructor
                    """)
    @PreAuthorize("(hasRole('ROLE_INSTRUCTOR')) or (hasRole('ROLE_ADMIN'))")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("Received request to delete a Lab Template");
        log.trace(String.format("Received request to delete a LabTemplate with (ID = %s)", id));

        labTemplateService.delete(id);

        log.trace(String.format("Fulfilled request to delete LabTemplate with (ID = %s", id));
        return ResponseEntity.noContent().build();
    }
}
