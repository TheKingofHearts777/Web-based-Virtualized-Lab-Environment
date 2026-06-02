package com.csproj.Cyberlab.API.VmTemplateTests;

import com.csproj.Cyberlab.API.exceptions.FileUploadException;
import com.csproj.Cyberlab.API.exceptions.NotFoundException;
import com.csproj.Cyberlab.API.exceptions.ObjectDeletionException;
import com.csproj.Cyberlab.API.user.UserService;
import com.csproj.Cyberlab.API.user.labInstance.vmInstance.VmInstance;
import com.csproj.Cyberlab.API.vmTemplate.VmTemplate;
import com.csproj.Cyberlab.API.vmTemplate.VmTemplateRepo;
import com.csproj.Cyberlab.API.vmTemplate.VmTemplateService;
import com.csproj.Cyberlab.API.virtualization.VirtualizationProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Date;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VmTemplateServiceTests {

    @Mock
    private VmTemplateRepo vmTemplateRepo;

    @Mock
    private UserService userService;

    @Mock
    private VirtualizationProvider virtualizationProvider;

    @InjectMocks
    private VmTemplateService vmTemplateService;

    /*
    Tests for findById
    */
    @Test
    void validMongoIdTest_Success() {
        String validId = UUID.randomUUID().toString();
        VmTemplate mockVmTemplate = new VmTemplate("template1", "test description", 101, "brodied");
        mockVmTemplate.setId(validId);

        when(vmTemplateRepo.findById(validId)).thenReturn(Optional.of(mockVmTemplate));

        VmTemplate result = vmTemplateService.findById(validId);

        assertNotNull(result);
        assertEquals(validId, result.getId());
        verify(vmTemplateRepo, times(1)).findById(validId);
    }

    @Test
    void invalidMongoIdTest_Failure() {
        String invalidId = UUID.randomUUID().toString();
        when(vmTemplateRepo.findById(invalidId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> vmTemplateService.findById(invalidId));

        assertEquals("Requested VmTemplate not found", exception.getMessage());
        verify(vmTemplateRepo, times(1)).findById(invalidId);
    }

    /*
    Tests for create
    */
    @Test
    void uploadVdiWithValidDataTest_Failure() throws FileUploadException {
        // Mocked InputStream for a file
        InputStream inputStream = new ByteArrayInputStream("mock data".getBytes());
        String name = "Valid Template";
        String description = "Test template description";

        VmTemplate mockTemplate = new VmTemplate(name, description, 123, "brodied");

        when(virtualizationProvider.createTemplate(any(InputStream.class), eq(name), eq(description)))
                .thenReturn(mockTemplate);
        when(vmTemplateRepo.save(any(VmTemplate.class))).thenReturn(mockTemplate);

        VmTemplate result = vmTemplateService.uploadVdi(inputStream, name, description);

        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(description, result.getDescription());
        verify(virtualizationProvider, times(1)).createTemplate(any(InputStream.class), eq(name), eq(description));
        verify(vmTemplateRepo, times(1)).save(mockTemplate);
    }

    @Test
    void uploadVdiWithFileUploadExceptionTest_Failure() throws FileUploadException {
        // Mocked InputStream for a file
        InputStream inputStream = new ByteArrayInputStream("mock data".getBytes());
        String name = "Invalid Template";
        String description = "This should fail";

        when(virtualizationProvider.createTemplate(any(InputStream.class), eq(name), eq(description)))
                .thenThrow(new FileUploadException("File upload failed"));

        FileUploadException exception = assertThrows(FileUploadException.class, () ->
                vmTemplateService.uploadVdi(inputStream, name, description));

        assertEquals("File upload failed", exception.getMessage());
        verify(virtualizationProvider, times(1)).createTemplate(any(InputStream.class), eq(name), eq(description));
        verify(vmTemplateRepo, never()).save(any(VmTemplate.class));
    }

    /*
    Tests for delete
    */
    @Test
    void deleteVmTemplateWithNoLinkedInstancesTest_Success() {
        String templateId = UUID.randomUUID().toString();
        VmTemplate mockTemplate = new VmTemplate("template1", "test description", 101, "brodied");
        mockTemplate.setId(templateId);

        when(userService.findVmInstancesByParentId(templateId)).thenReturn(Collections.emptyList());
        when(vmTemplateRepo.findById(templateId)).thenReturn(Optional.of(mockTemplate));

        doNothing().when(virtualizationProvider).deleteTemplate(mockTemplate.getProxmoxId());
        doNothing().when(vmTemplateRepo).deleteById(templateId);

        assertDoesNotThrow(() -> vmTemplateService.delete(templateId));

        verify(userService, times(1)).findVmInstancesByParentId(templateId);
        verify(vmTemplateRepo, times(1)).findById(templateId);
        verify(virtualizationProvider, times(1)).deleteTemplate(mockTemplate.getProxmoxId());
        verify(vmTemplateRepo, times(1)).deleteById(templateId);
    }

    @Test
    void deleteVmTemplateWhenNotFoundTest_Failure() {
        String templateId = UUID.randomUUID().toString();

        when(vmTemplateRepo.findById(templateId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> vmTemplateService.delete(templateId));

        assertEquals("VM Template not found", exception.getMessage());
        verify(vmTemplateRepo, times(1)).findById(templateId);
        verify(virtualizationProvider, never()).deleteTemplate(anyInt());
        verify(vmTemplateRepo, never()).deleteById(anyString());
    }

    @Test
    void deleteVmTemplateWithLinkedVmInstancesTest_Failure() {
        String templateId = UUID.randomUUID().toString();
        VmTemplate mockTemplate = new VmTemplate("template1", "test description", 101, "brodied");
        mockTemplate.setId(templateId);

        when(userService.findVmInstancesByParentId(templateId))
                .thenReturn(List.of(new VmInstance(1001, "brodied", "Clone-1001", new Date(System.currentTimeMillis()), "template1")));

        ObjectDeletionException exception = assertThrows(ObjectDeletionException.class,
                () -> vmTemplateService.delete(templateId));

        assertEquals("Found 1 VM Instances linked to VM Template " + templateId, exception.getMessage());

        verify(userService, times(1)).findVmInstancesByParentId(templateId);
        verify(vmTemplateRepo, never()).findById(anyString());
        verify(virtualizationProvider, never()).deleteTemplate(anyInt());
        verify(vmTemplateRepo, never()).deleteById(anyString());
    }
}
