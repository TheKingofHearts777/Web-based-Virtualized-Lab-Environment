package com.csproj.Cyberlab.API.user.labInstance.vmInstance;

import com.csproj.Cyberlab.API.user.ExpirationRecord;
import com.csproj.Cyberlab.API.user.UserService;
import com.csproj.Cyberlab.API.user.labInstance.LabInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class VmGarbageCollectorService {
    private final UserService userService;

    // Schedule to run at 2 AM daily (low-traffic time)
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredVMs() {
        log.info("Starting VM Garbage Collection Routine...");

        // Define the expiration threshold (1 week past due date)
        Date expirationThreshold = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7));

        // Retrieve expired lab instances as expiration records
        List<ExpirationRecord> expiredLabs = userService.getExpiredLabInstances(expirationThreshold);

        if (expiredLabs.isEmpty()) {
            log.info("No expired labs found for cleanup.");
            return;
        }

        // Iterate over each expiration record (each user and their expired labs)
        for (ExpirationRecord record : expiredLabs) {
            String userId = record.getUserId();
            // Iterate over each expired LabInstance for the user
            for (LabInstance lab : record.getLabs()) {
                Map<String, VmInstance> vmInstances = lab.getVmInstances();

                if (vmInstances == null || vmInstances.isEmpty()) {
                    log.warn("LabInstance {} has no associated VMs. Skipping.", lab.getId());
                    continue;
                }

                // Find VMs whose clone date is before the expiration threshold (expired VMs)
                List<String> expiredVmIds = vmInstances.entrySet().stream()
                        .filter(entry -> entry.getValue().getVmCloneDate().before(expirationThreshold))
                        .map(Map.Entry::getKey)
                        .sorted()
                        .toList();

                if (expiredVmIds.isEmpty()) {
                    log.info("LabInstance {} has only active VMs. Skipping cleanup.", lab.getId());
                    continue;
                }

                for (String vmId : expiredVmIds) {
                    try {
                        // Delete the VM using the userService which handles Proxmox deletion, lab update, and persistence
                        userService.deleteVmInstance(userId, lab.getId(), vmId);
                        log.info("Successfully deleted VM {} for LabInstance {}", vmId, lab.getId());
                    } catch (Exception e) {
                        log.error("Failed to delete VM {} for LabInstance {}. Continuing...", vmId, lab.getId(), e);
                    }
                }
            }
        }

        log.info("VM Garbage Collection Routine completed.");
    }
}