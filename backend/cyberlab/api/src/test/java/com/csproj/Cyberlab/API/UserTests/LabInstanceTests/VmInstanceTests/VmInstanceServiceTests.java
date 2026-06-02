package com.csproj.Cyberlab.API.UserTests.LabInstanceTests.VmInstanceTests;

import com.csproj.Cyberlab.API.auth.AuthService;
import com.csproj.Cyberlab.API.exceptions.NotFoundException;
import com.csproj.Cyberlab.API.user.labInstance.LabInstance;
import com.csproj.Cyberlab.API.user.labInstance.LabInstanceQuestion;
import com.csproj.Cyberlab.API.user.labInstance.vmInstance.VmInstance;
import com.csproj.Cyberlab.API.user.labInstance.vmInstance.VmInstanceService;
import com.csproj.Cyberlab.API.user.labInstance.vmInstance.VncConnectionResponse;
import com.csproj.Cyberlab.API.virtualization.VirtualizationProvider;
import com.csproj.Cyberlab.API.vmTemplate.VmTemplate;
import com.csproj.Cyberlab.API.vmTemplate.VmTemplateRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VmInstanceServiceTests {

    @Mock
    private VmTemplateRepo vmTemplateRepo;

    @Mock
    private VirtualizationProvider virtualizationProvider;

    @InjectMocks
    private VmInstanceService vmInstanceService;

    private LabInstance mockLabInstance;
    private VmInstance mockVmInstance;
    private VmTemplate mockVmTemplate;
    private String vmId;
    private String templateId;
    private int proxmoxVmId;

    @BeforeEach
    void setUp() {
        vmId = UUID.randomUUID().toString();
        templateId = UUID.randomUUID().toString();
        proxmoxVmId = 1001;

        mockVmInstance = new VmInstance(proxmoxVmId,
                "testUser",
                "TestVM",
                new Date(),
                "parentTemplateId"
        );

        mockVmInstance.setId(vmId);

        Map<String, VmInstance> vmInstances = new HashMap<>();
        vmInstances.put(mockVmInstance.getId(), mockVmInstance);

        mockLabInstance = new LabInstance(
                "Lab Template",
                "Test LabTemplate",
                new ArrayList<String>(),
                new HashMap<String, LabInstanceQuestion>(),
                "course1",
                "template1",
                new Date(),
                vmInstances,
                new ArrayList<>(),
                false,
                new Date()
        );

        mockLabInstance.setId("lab123");

        mockVmTemplate = new VmTemplate(
                "Test Template",
                "description",
                proxmoxVmId,
                "testTemplate"
        );

        mockVmTemplate.setId(templateId);
    }

    @Test
    void findById_Success() {
        VmInstance foundVm = vmInstanceService.findById(mockLabInstance, vmId);

        assertNotNull(foundVm);
        assertEquals(vmId, foundVm.getId());
    }

    @Test
    void findById_NotFound() {
        Exception exception = assertThrows(NotFoundException.class, () ->
                vmInstanceService.findById(mockLabInstance, "invalidVmId"));

        assertEquals("Requested VmInstance not found", exception.getMessage());
    }

    @Test
    void createVmInstance_Success() {
        when(vmTemplateRepo.findById("parentTemplateId")).thenReturn(Optional.of(mockVmTemplate));
        when(virtualizationProvider.createInstance(mockVmTemplate)).thenReturn(mockVmInstance);

        VmInstance createdVm = vmInstanceService.create("parentTemplateId");

        assertNotNull(createdVm);
        verify(vmTemplateRepo, times(1)).findById("parentTemplateId");
        verify(virtualizationProvider, times(1)).createInstance(mockVmTemplate);
    }

    @Test
    void createVmInstance_TemplateNotFound() {
        when(vmTemplateRepo.findById("invalidTemplateId")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> vmInstanceService.create("invalidTemplateId"));

        verify(vmTemplateRepo, times(1)).findById("invalidTemplateId");
    }

    @Test
    void deleteVmInstance_Success() {
        doNothing().when(virtualizationProvider).deleteInstance(proxmoxVmId);

        VmInstance deletedVm = vmInstanceService.delete(mockLabInstance, vmId);

        assertNotNull(deletedVm);
        assertEquals(vmId, deletedVm.getId());
        verify(virtualizationProvider, times(1)).deleteInstance(proxmoxVmId);
    }

    @Test
    void deleteVmInstance_NotFound() {
        assertThrows(NotFoundException.class, () -> vmInstanceService.delete(mockLabInstance, "invalidVmId"));
        verify(virtualizationProvider, never()).deleteInstance(anyInt());
    }

    @Test
    void getVmInstanceConnString_Success() {
        VncConnectionResponse mockResponse = new VncConnectionResponse("vnc://test-connection", "Noneofyabusiness", "5901");
        when(virtualizationProvider.getConnectionURI(String.valueOf(proxmoxVmId))).thenReturn(mockResponse);

        VncConnectionResponse response = vmInstanceService.getConnString(mockLabInstance, vmId);

        assertNotNull(response);
        assertEquals("vnc://test-connection", response.getVncWebSocketConnection());
        assertEquals("Noneofyabusiness", response.getPveTicketCookie());
        assertEquals("5901", response.getVncPort());

        verify(virtualizationProvider, times(1)).getConnectionURI(String.valueOf(proxmoxVmId));
    }

    @Test
    void getVmInstanceConnString_NotFound() {
        assertThrows(NotFoundException.class, () -> vmInstanceService.getConnString(mockLabInstance, "invalidVmId"));

        verify(virtualizationProvider, never()).getConnectionURI(anyString());
    }
}
