package com.csproj.Cyberlab.API.UserTests.LabInstanceTests;

import com.csproj.Cyberlab.API.course.Course;
import com.csproj.Cyberlab.API.course.CourseService;
import com.csproj.Cyberlab.API.exceptions.ForbiddenException;
import com.csproj.Cyberlab.API.exceptions.InternalServerException;
import com.csproj.Cyberlab.API.exceptions.NotFoundException;
import com.csproj.Cyberlab.API.exceptions.ObjectDeletionException;
import com.csproj.Cyberlab.API.labTemplate.LabTemplate;
import com.csproj.Cyberlab.API.labTemplate.LabTemplateRepo;
import com.csproj.Cyberlab.API.user.User;
import com.csproj.Cyberlab.API.user.UserType;
import com.csproj.Cyberlab.API.user.labInstance.LabInstance;
import com.csproj.Cyberlab.API.user.labInstance.LabInstanceQuestion;
import com.csproj.Cyberlab.API.user.labInstance.LabInstanceService;
import com.csproj.Cyberlab.API.user.labInstance.LabInstanceUpdateDTO;
import com.csproj.Cyberlab.API.user.labInstance.vmInstance.VmInstance;
import com.csproj.Cyberlab.API.user.labInstance.vmInstance.VmInstanceService;
import com.csproj.Cyberlab.API.user.labInstance.vmInstance.VncConnectionResponse;
import com.csproj.Cyberlab.API.virtualization.VirtualizationProvider;
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
public class LabInstanceServiceTests {

    @Mock
    private LabTemplateRepo labTemplateRepo;

    @Mock
    private VmInstanceService vmInstanceService;

    @Mock
    private CourseService courseService;

    @Mock
    private VirtualizationProvider virtualizationProvider;

    @InjectMocks
    private LabInstanceService labInstanceService;

    private User mockUser;
    private LabInstance mockLabInstance;
    private VmInstance mockVmInstance;
    private LabTemplate mockLabTemplate;
    private Course mockCourse;

    @BeforeEach
    void setUp() {
        mockUser = new User(
                "brodied@uwplatt.edu",
                "password",
                UserType.Admin
        );

        mockUser.setId("user123");

        mockVmInstance = new VmInstance(
                1001,
                "brodied",
                "VM-1",
                new Date(),
                "template123"
        );

        mockVmInstance.setId("vm123");

        mockLabInstance = new LabInstance(
                "Lab Template",
                "Test LabTemplate",
                new ArrayList<String>(),
                new HashMap<String, LabInstanceQuestion>(),
                "course1",
                "template1",
                new Date(),
                new HashMap<>(),
                new ArrayList<>(),
                false,
                new Date()
        );

        mockLabInstance.setId("lab123");

        HashMap<String, LabInstance> labInstances = new HashMap<>();
        labInstances.put(mockLabInstance.getId(), mockLabInstance);

        mockUser.setLabInstances(labInstances);

        mockLabTemplate = new LabTemplate(
                "Lab Template",
                "description",
                new HashMap<>(),
                List.of(),
                List.of("template123")
        );

        mockLabTemplate.setId("template1");

        mockCourse = new Course("Senior Design Project: 2");
        mockCourse.setId("course1");
    }

    @Test
    void findById_Success() {
        LabInstance foundLab = labInstanceService.findById(mockUser, "lab123");

        assertNotNull(foundLab);
        assertEquals("lab123", foundLab.getId());
    }

    @Test
    void findById_Failure() {
        Exception exception = assertThrows(NotFoundException.class, () -> labInstanceService.findById(mockUser, "invalidId"));
        assertEquals("Requested LabInstance not found", exception.getMessage());
    }

    @Test
    void updateLabInstance_Success() {
        LabInstanceUpdateDTO updateDTO = new LabInstanceUpdateDTO(List.of("Answer 1", "Answer 2"));

        LabInstance updatedLab = labInstanceService.update(mockUser, "lab123", updateDTO);

        assertNotNull(updatedLab);
        assertTrue(updatedLab.isCompleted());
        assertEquals(updateDTO.userAnswers(), updatedLab.getUserAnswers());
    }

    @Test
    void updateLabInstance_AlreadyCompleted() {
        mockLabInstance.setCompleted(true);
        LabInstanceUpdateDTO updateDTO = new LabInstanceUpdateDTO(List.of("Answer 1", "Answer 2"));

        assertThrows(ForbiddenException.class, () -> labInstanceService.update(mockUser, "lab123", updateDTO));
    }

    @Test
    void removeVmInstance_Success() {
        when(vmInstanceService.delete(mockLabInstance, "vm123")).thenReturn(mockVmInstance);

        LabInstance updatedLab = labInstanceService.removeVmInstance(mockUser, "lab123", "vm123");

        assertNotNull(updatedLab);
        assertTrue(updatedLab.getVmInstances().isEmpty());
        verify(vmInstanceService, times(1)).delete(mockLabInstance, "vm123");
    }

    @Test
    void removeVmInstance_Failure() {
        doThrow(new ObjectDeletionException("Failed to delete VM instance"))
                .when(vmInstanceService).delete(mockLabInstance, "vm123");

        assertThrows(ObjectDeletionException.class, () -> labInstanceService.removeVmInstance(mockUser, "lab123", "vm123"));

        verify(vmInstanceService, times(1)).delete(mockLabInstance, "vm123");
    }

    @Test
    void createLabInstance_Success() {
        when(labTemplateRepo.findById("template1")).thenReturn(Optional.of(mockLabTemplate));
        when(courseService.findById("course1")).thenReturn(mockCourse);
        when(vmInstanceService.create("template123")).thenReturn(mockVmInstance);

        Map<String, VmInstance> vmInstances = new HashMap<>();
        vmInstances.put("vm123", mockVmInstance);
        doNothing().when(virtualizationProvider).addLabInstanceNetworking(vmInstances);

        LabInstance createdLab = labInstanceService.create("template1", "course1", new Date());

        assertNotNull(createdLab);
        assertEquals("Lab Template", createdLab.getTemplateName());
        assertEquals("course1", createdLab.getCourseId());
        assertEquals(1, createdLab.getVmInstances().size());
    }

    @Test
    void createLabInstance_LabTemplateNotFound() {
        when(labTemplateRepo.findById("invalidId")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> labInstanceService.create("invalidId", "course1", new Date()));
    }

    @Test
    void createLabInstance_CourseNotFound() {
        when(labTemplateRepo.findById("template1")).thenReturn(Optional.of(mockLabTemplate));
        when(courseService.findById("invalidCourse")).thenThrow(new NotFoundException("Course not found", null));

        assertThrows(NotFoundException.class, () -> labInstanceService.create("template1", "invalidCourse", new Date()));
    }

    @Test
    void findVmInstanceById_Success() {
        when(vmInstanceService.findById(mockLabInstance, "vm123")).thenReturn(mockVmInstance);

        VmInstance foundVm = labInstanceService.findVmInstanceById(mockUser, "lab123", "vm123");

        assertNotNull(foundVm);
        assertEquals("vm123", foundVm.getId());
    }

    @Test
    void findVmInstanceById_Failure() {
        when(vmInstanceService.findById(mockLabInstance, "invalidVm")).thenThrow(new NotFoundException("VM not found", null));

        assertThrows(NotFoundException.class, () -> labInstanceService.findVmInstanceById(mockUser, "lab123", "invalidVm"));
    }

    @Test
    void getVmInstanceConnString_Success() {
        VncConnectionResponse mockResponse = new VncConnectionResponse("ws://localhost:8080", "some-token", "8080");

        when(vmInstanceService.getConnString(mockLabInstance, "vm123")).thenReturn(mockResponse);

        VncConnectionResponse response = labInstanceService.getVmInstanceConnString(mockUser, "lab123", "vm123");

        assertNotNull(response);
        assertEquals("ws://localhost:8080", response.getVncWebSocketConnection());
        assertEquals("some-token", response.getPveTicketCookie());
        assertEquals("8080", response.getVncPort());

        verify(vmInstanceService, times(1)).getConnString(mockLabInstance, "vm123");
    }

    @Test
    void getVmInstanceConnString_Failure() {
        when(vmInstanceService.getConnString(mockLabInstance, "invalidVm")).thenThrow(new NotFoundException("VM Connection Not Found", null));

        assertThrows(NotFoundException.class, () -> labInstanceService.getVmInstanceConnString(mockUser, "lab123", "invalidVm"));
    }
}
