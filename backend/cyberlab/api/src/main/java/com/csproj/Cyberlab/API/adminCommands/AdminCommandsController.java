package com.csproj.Cyberlab.API.adminCommands;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/adminCommands")
@RequiredArgsConstructor
@Tag(name="Admin Commands", description = """
        **Authorized UserTypes:**
        - Admin
        
        **Note:**
        - This module provides experimental endpoints from administrators to manage system resources.
        - These endpoints should be invoked only with extreme caution.
        """)
@Slf4j
public class AdminCommandsController {
    private final AdminCommandsService adminCommandsService;

    /**
     * Endpoint for hard reset
     * In the future, check if user who called it is UserType.ADMIN
     *
     * @return ResponseEntity
     */
    @DeleteMapping("/hardReset")
    @Operation(summary = "Deletes all non-root VMs and labs from MongoDB and Proxmox server. Only accessible to admins.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> hardReset() {
        log.trace("Fulfilled request to delete all non-root VMs and labs.");

        adminCommandsService.hardReset();

        return ResponseEntity.noContent().build();
    }
}
