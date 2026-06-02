package com.csproj.Cyberlab.API.UserTests.LabInstanceTests.VmInstanceTests;

import com.csproj.Cyberlab.API.exceptions.ObjectDeletionException;
import com.csproj.Cyberlab.API.user.ExpirationRecord;
import com.csproj.Cyberlab.API.user.UserService;
import com.csproj.Cyberlab.API.user.labInstance.LabInstance;
import com.csproj.Cyberlab.API.user.labInstance.LabInstanceQuestion;
import com.csproj.Cyberlab.API.user.labInstance.vmInstance.VmGarbageCollectorService;
import com.csproj.Cyberlab.API.user.labInstance.vmInstance.VmInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VmGarbageCollectorServiceTests {

    @Mock
    private UserService userService;

    @InjectMocks
    private VmGarbageCollectorService vmGarbageCollectorService;

    private LabInstance mockLabInstance;
    private VmInstance expiredVm1;
    private VmInstance expiredVm2;
    private VmInstance activeVm;

    private String userId = "user123";

    @BeforeEach
    void setUp() {
        mockLabInstance = new LabInstance(
                "template1",
                "Test LabInstance",
                new ArrayList<String>(),
                new HashMap<String, LabInstanceQuestion>(),
                "course1",
                "templateId",
                new Date(),
                new HashMap<>(),
                List.of(),
                false,
                new Date()
        );
        mockLabInstance.setId("lab123");

        expiredVm1 = new VmInstance(
                1001,
                "brodied",
                "expiredVM1",
                new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(8)),
                "vmTemplateId"
        );
        expiredVm1.setId("1001");

        expiredVm2 = new VmInstance(
                1002,
                "brodied",
                "expiredVM2",
                new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(9)),
                "vmTemplateId"
        );
        expiredVm2.setId("1002");

        activeVm = new VmInstance(
                1003,
                "brodied",
                "activeVM",
                new Date(System.currentTimeMillis()),
                "vmTemplateId"
        );
        activeVm.setId("1003");

        Map<String, VmInstance> vmMap = new HashMap<>();
        vmMap.put(expiredVm1.getId(), expiredVm1);
        vmMap.put(expiredVm2.getId(), expiredVm2);
        vmMap.put(activeVm.getId(), activeVm);

        mockLabInstance.setVmInstances(vmMap);
    }

    @Test
    void cleanupExpiredVMs_Success() {
        ExpirationRecord record = new ExpirationRecord(userId, List.of(mockLabInstance));
        when(userService.getExpiredLabInstances(any(Date.class))).thenReturn(List.of(record));

        assertDoesNotThrow(() -> vmGarbageCollectorService.cleanupExpiredVMs());

        String labId = mockLabInstance.getId();
        verify(userService).getExpiredLabInstances(any(Date.class));
        verify(userService).deleteVmInstance(userId, labId, expiredVm1.getId());
        verify(userService).deleteVmInstance(userId, labId, expiredVm2.getId());
        verify(userService, never()).deleteVmInstance(userId, labId, activeVm.getId());
    }

    @Test
    void cleanupExpiredVMs_NoExpiredLabs() {
        when(userService.getExpiredLabInstances(any(Date.class))).thenReturn(List.of());

        assertDoesNotThrow(() -> vmGarbageCollectorService.cleanupExpiredVMs());

        verify(userService).getExpiredLabInstances(any(Date.class));
        verify(userService, never()).deleteVmInstance(anyString(), anyString(), anyString());
    }

    @Test
    void cleanupExpiredVMs_ExceptionOnDelete() {
        ExpirationRecord record = new ExpirationRecord(userId, List.of(mockLabInstance));

        when(userService.getExpiredLabInstances(any(Date.class))).thenReturn(List.of(record));

        doThrow(new ObjectDeletionException("Failed to detele VM 1001"))
                .when(userService).deleteVmInstance(userId, "lab123", "1001");

        assertDoesNotThrow(() -> vmGarbageCollectorService.cleanupExpiredVMs());

        String labId = mockLabInstance.getId();
        verify(userService).deleteVmInstance(userId, labId, expiredVm1.getId());
        verify(userService).deleteVmInstance(userId, labId, expiredVm2.getId());
        verify(userService, never()).deleteVmInstance(userId, labId, activeVm.getId());
    }
}
