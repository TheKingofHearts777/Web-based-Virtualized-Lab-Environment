package com.csproj.Cyberlab.API.UserTests;

import com.csproj.Cyberlab.API.auth.AuthService;
import com.csproj.Cyberlab.API.exceptions.BadRequestException;
import com.csproj.Cyberlab.API.exceptions.NotFoundException;
import com.csproj.Cyberlab.API.user.User;
import com.csproj.Cyberlab.API.user.UserRepo;
import com.csproj.Cyberlab.API.user.UserService;
import com.csproj.Cyberlab.API.user.UserType;
import com.csproj.Cyberlab.API.user.*;
import com.csproj.Cyberlab.API.user.labInstance.*;
import com.csproj.Cyberlab.API.user.labInstance.vmInstance.VmInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @Mock
    private UserRepo userRepo;

    @Mock
    private LabInstanceService labInstanceService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private UserService userService;

    private User mockUser;
    private LabInstance mockLabInstance;
    private VmInstance mockVmInstance;

    @BeforeEach
    void setUp() {
        mockUser = new User(
                "username",
                "123",
                UserType.Student
        );

        mockLabInstance = new LabInstance(
                "labInstance1",
                "Test LabInstance",
                new ArrayList<String>(),
                new HashMap<String, LabInstanceQuestion>(),
                "course1",
                "template1",
                new Date(System.currentTimeMillis()),
                new HashMap<>(),
                List.of(),
                false,
                new Date(System.currentTimeMillis())
        );

        mockVmInstance = new VmInstance(
                1001,
                "brodied",
                "Clone-1001",
                new Date(System.currentTimeMillis()),
                "vmParentId"
        );

        mockUser.setId("123");
        mockLabInstance.setId("lab1");
        mockVmInstance.setId("Clone-1001");

        Map<String, VmInstance> vms = mockLabInstance.getVmInstances();
        vms.put(mockVmInstance.getId(), mockVmInstance);
        mockLabInstance.setVmInstances(vms);

        Map<String, LabInstance> labs = mockUser.getLabInstances();
        labs.put(mockLabInstance.getId(), mockLabInstance);
        mockUser.setLabInstances(labs);
    }

    @Test
    void validMongoIdTest_Success() {
        when(userRepo.findById("123")).thenReturn(Optional.of(mockUser));
        doNothing().when(authService).authorizeResourceAccess(anyString());

        User result = userService.findById("123");

        assertNotNull(result);
        assertEquals("123", result.getId());

        verify(userRepo, times(1)).findById("123");
    }

    @Test
    void invalidMongoIdTest_Failure() {
        when(userRepo.findById("999")).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.findById("999"));

        assertEquals("Requested User not found", exception.getMessage());
        verify(userRepo, times(1)).findById("999");
    }

    @Test
    void findLabInstanceByIdTest_Success() {
        when(userRepo.findById("123")).thenReturn(Optional.of(mockUser));
        when(labInstanceService.findById(mockUser, "lab1")).thenReturn(mockLabInstance);
        doNothing().when(authService).authorizeResourceAccess(anyString());

        LabInstance result = userService.findLabInstanceById("123", "lab1");

        assertNotNull(result);
        assertEquals("lab1", result.getId());

        verify(userRepo, times(1)).findById("123");
        verify(labInstanceService, times(1)).findById(mockUser, "lab1");
    }

    @Test
    void getExpiredLabInstancesTest_Success() {
        Date expirationThreshold = new Date(System.currentTimeMillis() - 100000);
        when(userRepo.findByExpiredLabInstance(expirationThreshold)).thenReturn(Collections.singletonList(mockUser));

        List<ExpirationRecord> result = userService.getExpiredLabInstances(expirationThreshold);

        System.out.println(result);

        assertFalse(result.isEmpty());
        assertEquals("lab1", result.getFirst().getLabs().getFirst().getId());

        verify(userRepo, times(1)).findByExpiredLabInstance(expirationThreshold);
    }

    @Test
    void createLabInstanceWithValidDataTest_Success() {
        LabInstanceCreateDTO dto = new LabInstanceCreateDTO();
        dto.setTemplateId("template2");
        dto.setCourseId("course1");
        dto.setDueDate(new Date(System.currentTimeMillis()));

        when(userRepo.findById("123")).thenReturn(Optional.of(mockUser));
        when(labInstanceService.create("template2", "course1", dto.getDueDate())).thenReturn(mockLabInstance);
        when(userRepo.save(any(User.class))).thenReturn(mockUser);
        doNothing().when(authService).authorizeResourceAccess(anyString());

        LabInstance result = userService.createLabInstance("123", dto);

        assertNotNull(result);
        assertEquals("lab1", result.getId());

        verify(userRepo, times(1)).findById("123");
        verify(labInstanceService, times(1)).create("template2", "course1", dto.getDueDate());
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    void createLabInstanceAlreadyExistsTest_Failure() {
        LabInstanceCreateDTO dto = new LabInstanceCreateDTO();
        dto.setTemplateId("template1");
        dto.setCourseId("course1");
        dto.setDueDate(new Date(System.currentTimeMillis()));

        when(userRepo.findById("123")).thenReturn(Optional.of(mockUser));
        BadRequestException exception = assertThrows(BadRequestException.class, () -> userService.createLabInstance("123", dto));

        assertEquals("User already has a LabInstance for the supplied Template", exception.getMessage());

        verify(userRepo, times(1)).findById("123");
        verify(labInstanceService, never()).create(any(), any(), any());
    }

    @Test
    void patchLabInstanceTest_Success() {
        List<String> userAnswers = new ArrayList<>();

        userAnswers.add("Answer 1");
        userAnswers.add("Answer 2");

        LabInstanceUpdateDTO dto = new LabInstanceUpdateDTO(userAnswers);
        when(userRepo.findById("123")).thenReturn(Optional.of(mockUser));
        when(labInstanceService.update(mockUser, "lab1", dto)).thenReturn(mockLabInstance);
        when(userRepo.save(any(User.class))).thenReturn(mockUser);
        doNothing().when(authService).authorizeResourceAccess(anyString());

        LabInstance result = userService.updateLabInstance("123", "lab1", dto);

        assertNotNull(result);
        assertEquals("lab1", result.getId());

        verify(userRepo, times(1)).findById("123");
        verify(labInstanceService, times(1)).update(mockUser, "lab1", dto);
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    void findVmInstanceByIdTest_Success() {
        when(userRepo.findById("123")).thenReturn(Optional.of(mockUser));
        when(labInstanceService.findVmInstanceById(mockUser, "lab1", "Clone-1001")).thenReturn(mockVmInstance);
        doNothing().when(authService).authorizeResourceAccess(anyString());

        VmInstance result = userService.findVmInstanceById("123", "lab1", "Clone-1001");

        assertNotNull(result);
        assertEquals("Clone-1001", result.getId());

        verify(userRepo, times(1)).findById("123");
        verify(labInstanceService, times(1)).findVmInstanceById(mockUser, "lab1", "Clone-1001");
    }

    @Test
    void deleteVmInstanceTest_Success() {
        when(userRepo.findById("123")).thenReturn(Optional.of(mockUser));
        when(labInstanceService.removeVmInstance(mockUser, "lab1", "Clone-1001")).thenReturn(mockLabInstance);
        when(userRepo.save(any(User.class))).thenReturn(mockUser);

        assertDoesNotThrow(() -> userService.deleteVmInstance("123", "lab1", "Clone-1001"));

        verify(userRepo, times(1)).findById("123");
        verify(labInstanceService, times(1)).removeVmInstance(mockUser, "lab1", "Clone-1001");
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    void deleteVmInstanceTest_Failure() {
        when(userRepo.findById("123")).thenReturn(Optional.of(mockUser));
        when(labInstanceService.removeVmInstance(mockUser, "lab1", "Clone-1001"))
                .thenThrow(new RuntimeException("Failed to remove VM instance"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.deleteVmInstance("123", "lab1", "Clone-1001"));

        assertEquals("Failed to remove VM instance", exception.getMessage());

        verify(userRepo, times(1)).findById("123");
        verify(labInstanceService, times(1)).removeVmInstance(mockUser, "lab1", "Clone-1001");
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void findLabInstanceByIdTest_Failure() {
        when(userRepo.findById("123")).thenReturn(Optional.of(mockUser));
        when(labInstanceService.findById(mockUser, "lab1")).thenThrow(new NotFoundException("LabInstance not found", null));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.findLabInstanceById("123", "lab1"));

        assertEquals("LabInstance not found", exception.getMessage());
        verify(userRepo, times(1)).findById("123");
        verify(labInstanceService, times(1)).findById(mockUser, "lab1");
    }

    @Test
    void getExpiredLabInstancesTest_Failure() {
        Date expirationThreshold = new Date(System.currentTimeMillis() - 10000);
        when(userRepo.findByExpiredLabInstance(expirationThreshold)).thenReturn(Collections.emptyList());

        List<ExpirationRecord> result = userService.getExpiredLabInstances(expirationThreshold);

        assertTrue(result.isEmpty());

        verify(userRepo, times(1)).findByExpiredLabInstance(expirationThreshold);
    }

    @Test
    void patchLabInstanceTest_Failure() {
        List<String> userAnswers = List.of("Answer 1", "Answer 2");
        LabInstanceUpdateDTO dto = new LabInstanceUpdateDTO(userAnswers);

        when(userRepo.findById("123")).thenReturn(Optional.of(mockUser));
        when(labInstanceService.update(mockUser, "lab1", dto))
                .thenThrow(new NotFoundException("LabInstance not found", null));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.updateLabInstance("123", "lab1", dto));

        assertEquals("LabInstance not found", exception.getMessage());

        verify(userRepo, times(1)).findById("123");
        verify(labInstanceService, times(1)).update(mockUser, "lab1", dto);
    }

    @Test
    void findVmInstanceByIdTest_Failure() {
        when(userRepo.findById("123")).thenReturn(Optional.of(mockUser));
        when(labInstanceService.findVmInstanceById(mockUser, "lab1", "Clone-1001"))
                .thenThrow(new NotFoundException("VmInstance not found", null));

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                userService.findVmInstanceById("123", "lab1", "Clone-1001"));

        assertEquals("VmInstance not found", exception.getMessage());

        verify(userRepo, times(1)).findById("123");
        verify(labInstanceService, times(1)).findVmInstanceById(mockUser, "lab1", "Clone-1001");
    }

    //TODO: VNC connection tests
}
