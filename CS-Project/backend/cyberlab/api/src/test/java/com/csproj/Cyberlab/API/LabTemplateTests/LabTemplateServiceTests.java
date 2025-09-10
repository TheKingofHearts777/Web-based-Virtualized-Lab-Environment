package com.csproj.Cyberlab.API.LabTemplateTests;

import com.csproj.Cyberlab.API.exceptions.NotFoundException;
import com.csproj.Cyberlab.API.labTemplate.LabTemplate;
import com.csproj.Cyberlab.API.labTemplate.LabTemplateRepo;
import com.csproj.Cyberlab.API.labTemplate.LabTemplateService;
import com.csproj.Cyberlab.API.vmTemplate.VmTemplateService;
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
public class LabTemplateServiceTests {
    @Mock
    private LabTemplateRepo labTemplateRepo;

    @Mock
    private VmTemplateService vmTemplateService;

    @InjectMocks
    private LabTemplateService labTemplateService;

    private LabTemplate mockLabTemplate;

    @BeforeEach
    void setUp() {
        mockLabTemplate = new LabTemplate(
                "Lab-Template1",
                "description",
                new HashMap<>(),
                List.of("Objectives"),
                List.of("vmTemplate1")
        );

        mockLabTemplate.setId(UUID.randomUUID().toString());
    }

    @Test
    void validMongoIdTest_Success() {
        when(labTemplateRepo.findById(mockLabTemplate.getId())).thenReturn(Optional.of(mockLabTemplate));

        LabTemplate foundLabTemplate = labTemplateService.findById(mockLabTemplate.getId());

        assertNotNull(foundLabTemplate);
        assertEquals(mockLabTemplate.getId(), foundLabTemplate.getId());
        verify(labTemplateRepo, times(1)).findById(mockLabTemplate.getId());
    }

    @Test
    void invalidMongoIdTest_Failure() {
        String invalidId = UUID.randomUUID().toString();
        when(labTemplateRepo.findById(invalidId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> labTemplateService.findById(invalidId));

        assertEquals("Requested LabTemplate not found", exception.getMessage());
        verify(labTemplateRepo, times(1)).findById(invalidId);
    }

    @Test
    void createLabTemplateWithValidDataTest_Success() {
        when(labTemplateRepo.save(any(LabTemplate.class))).thenReturn(mockLabTemplate);

        LabTemplate createdLabTemplate = labTemplateService.create(mockLabTemplate);

        assertNotNull(createdLabTemplate);
        assertEquals(mockLabTemplate.getName(), createdLabTemplate.getName());

        for (String vmTemplateId : mockLabTemplate.getVmTemplateIds()) {
            verify(vmTemplateService, times(1)).findById(vmTemplateId);
        }
        verify(labTemplateRepo, times(1)).save(mockLabTemplate);
    }

    @Test
    void createLabTemplateWithInvalidDataTest_Failure() {
        LabTemplate invalidLabTemplate = new LabTemplate(
                "",
                "",
                null,
                List.of(),
                List.of()
        );

        when(labTemplateRepo.save(any(LabTemplate.class)))
                .thenThrow(new IllegalArgumentException("Invalid LabTemplate data"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            labTemplateService.create(invalidLabTemplate);
        });

        assertEquals("Invalid LabTemplate data", exception.getMessage());

        verify(labTemplateRepo, times(1)).save(any(LabTemplate.class));
    }

    @Test
    void deleteLabTemplateWithValidDataTest_Success() {
        when(labTemplateRepo.findById(mockLabTemplate.getId())).thenReturn(Optional.of(mockLabTemplate));

        assertDoesNotThrow(() -> labTemplateService.delete(mockLabTemplate.getId()));

        verify(labTemplateRepo, times(1)).deleteById(mockLabTemplate.getId());
    }

    @Test
    void deleteLabTemplateWithInvalidDataTest_Failure() throws NotFoundException {
        String invalidId = UUID.randomUUID().toString();
        when(labTemplateRepo.findById(invalidId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> labTemplateService.delete(invalidId));

        assertEquals("LabTemplate not found: " + invalidId, exception.getMessage());
        verify(labTemplateRepo, never()).deleteById(anyString());
    }
}
