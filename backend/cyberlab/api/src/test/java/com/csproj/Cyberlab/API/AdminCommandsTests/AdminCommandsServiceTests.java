package com.csproj.Cyberlab.API.AdminCommandsTests;

import com.csproj.Cyberlab.API.adminCommands.AdminCommandsService;
import com.csproj.Cyberlab.API.course.Course;
import com.csproj.Cyberlab.API.course.CourseRepo;
import com.csproj.Cyberlab.API.course.CourseService;
import com.csproj.Cyberlab.API.labTemplate.LabTemplate;
import com.csproj.Cyberlab.API.labTemplate.LabTemplateRepo;
import com.csproj.Cyberlab.API.labTemplate.LabTemplateService;
import com.csproj.Cyberlab.API.user.User;
import com.csproj.Cyberlab.API.user.UserRepo;
import com.csproj.Cyberlab.API.user.UserService;
import com.csproj.Cyberlab.API.user.UserType;
import com.csproj.Cyberlab.API.user.labInstance.LabInstance;
import com.csproj.Cyberlab.API.user.labInstance.LabInstanceQuestion;
import com.csproj.Cyberlab.API.user.labInstance.vmInstance.VmInstance;
import com.csproj.Cyberlab.API.user.labInstance.vmInstance.VmInstanceService;
import com.csproj.Cyberlab.API.vmTemplate.VmTemplate;
import com.csproj.Cyberlab.API.vmTemplate.VmTemplateRepo;
import com.csproj.Cyberlab.API.vmTemplate.VmTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminCommandsServiceTests {

    @Mock
    private VmInstanceService vmInstanceService;

    @Mock
    private VmTemplateService vmTemplateService;

    @Mock
    private VmTemplateRepo vmTemplateRepo;

    @Mock
    private LabTemplateService labTemplateService;

    @Mock
    private LabTemplateRepo labTemplateRepo;

    @Mock
    private UserService userService;

    @Mock
    private UserRepo userRepo;

    @Mock
    private CourseService courseService;

    @Mock
    private CourseRepo courseRepo;

    @InjectMocks
    private AdminCommandsService adminCommandsService;

    private User mockInstructor;
    private User mockStudent;
    private User mockAdmin;
    private VmTemplate mockVmTemplate;
    private LabTemplate mockLabTemplate;
    private LabInstance mockLabInstance;
    private VmInstance mockVmInstance;
    private Course mockCourse;

    @BeforeEach
    void setUp() {
        mockInstructor = new User(
                "testinstructor@example.com",
                "user123",
                UserType.Instructor
        );
        mockInstructor.setId("user123");

        mockStudent = new User(
                "teststudent@example.com",
                "user456",
                UserType.Student
        );
        mockStudent.setId("user456");

        mockAdmin = new User(
                "testadmin@example.com",
                "user789",
                UserType.Admin
        );
        mockAdmin.setId("user789");

        mockVmTemplate = new VmTemplate(
                "vmTemplate123",
                "Test VM Template",
                101,
                "owner123"
        );
        mockVmTemplate.setId("vmTemplate123");

        mockLabTemplate = new LabTemplate(
                "labTemplate123",
                "Test Lab Template",
                new HashMap<>(),
                List.of(),
                List.of(mockVmTemplate.getId())
        );
        mockLabTemplate.setId("labTemplate123");

        mockLabInstance = new LabInstance(
                "labInstance123",
                "Test Labinstance",
                new ArrayList<String>(),
                new HashMap<String, LabInstanceQuestion>(),
                "course123",
                mockLabTemplate.getId(),
                new java.sql.Date(System.currentTimeMillis()),
                new HashMap<>(),
                List.of(),
                false,
                new java.util.Date(System.currentTimeMillis())
        );
        mockLabInstance.setId("labInstance123");

        mockVmInstance = new VmInstance(
                1001,
                "vmOwner123",
                "Clone-1001",
                new java.util.Date(System.currentTimeMillis()),
                "vmParent123"
        );
        mockVmInstance.setId("vmInstance123");

        mockCourse = new Course(
            "Senior Design"
        );
        mockCourse.setId("course123");

        Map<String, VmInstance> vms = mockLabInstance.getVmInstances();
        vms.put(mockVmInstance.getId(), mockVmInstance);
        mockLabInstance.setVmInstances(vms);

        Map<String, LabInstance> labs = mockInstructor.getLabInstances();
        labs.put(mockLabInstance.getId(), mockLabInstance);
        mockInstructor.setLabInstances(labs);

        List<String> instructors = mockCourse.getInstructors();
        instructors.add(mockInstructor.getId());
        mockCourse.setInstructors(instructors);

        List<String> students = mockCourse.getStudents();
        students.add(mockStudent.getId());
        mockCourse.setStudents(students);
    }

    @Test
    void hardReset_Success() {
        when(userRepo.findAll()).thenReturn(List.of(mockInstructor, mockStudent, mockAdmin));
        when(vmTemplateRepo.findAll()).thenReturn(List.of(mockVmTemplate));
        when(labTemplateRepo.findAll()).thenReturn(List.of(mockLabTemplate));
        when(courseRepo.findAll()).thenReturn(List.of(mockCourse));

        adminCommandsService.hardReset();

        verify(vmInstanceService, times(1)).delete(mockLabInstance, mockVmInstance.getId());

        verify(vmTemplateService, times(1)).delete(mockVmTemplate.getId());

        verify(userService, times(1)).deleteLabInstance(mockInstructor, mockLabInstance);

        verify(labTemplateService, times(1)).delete(mockLabTemplate.getId());

        verify(userService, times(1)).deleteUser(mockInstructor.getId());

        verify(userService, times(1)).deleteUser(mockStudent.getId());

        verify(userService, times(0)).deleteUser(mockAdmin.getId());

        verify(courseService, times(1)).deleteCourse(mockCourse.getId());

        verify(userRepo, times(1)).findAll();
        verify(vmTemplateRepo, times(1)).findAll();
        verify(labTemplateRepo, times(1)).findAll();
        verify(courseRepo, times(1)).findAll();
    }
}
