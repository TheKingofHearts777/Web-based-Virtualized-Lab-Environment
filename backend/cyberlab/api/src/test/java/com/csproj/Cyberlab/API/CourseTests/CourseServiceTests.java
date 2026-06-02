package com.csproj.Cyberlab.API.CourseTests;

import com.csproj.Cyberlab.API.auth.AuthService;
import com.csproj.Cyberlab.API.course.Course;
import com.csproj.Cyberlab.API.course.CourseRepo;
import com.csproj.Cyberlab.API.course.CourseService;
import com.csproj.Cyberlab.API.exceptions.NotFoundException;
import com.csproj.Cyberlab.API.exceptions.ObjectDeletionException;
import com.csproj.Cyberlab.API.user.User;
import com.csproj.Cyberlab.API.user.UserType;
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
public class CourseServiceTests {

    @InjectMocks
    CourseService courseService;

    @Mock
    CourseRepo courseRepo;

    @Mock
    AuthService authService;

    private Course mockCourse;

    @BeforeEach
    void setUp() {
        mockCourse = new Course(
                "Mock Course"
        );

        mockCourse.setId(UUID.randomUUID().toString());
    }

    @Test
    void validMongoIdTest_Success() {
        when(courseRepo.findById(mockCourse.getId())).thenReturn(Optional.of(mockCourse));
        doNothing().when(authService).authorizeResourceAccess(anyList());

        Course course = courseService.findById(mockCourse.getId());

        assertNotNull(course);
        assertEquals(course, mockCourse);
        verify(courseRepo, times(1)).findById(mockCourse.getId());
    }

    @Test
    void invalidMongoIdTest_Failure() {
        String invalidId = UUID.randomUUID().toString();
        when(courseRepo.findById(invalidId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> courseService.findById(invalidId));

        assertEquals("Requested Course not found", ex.getMessage());
        verify(courseRepo, times(1)).findById(invalidId);
    }

    @Test
    void deleteCourseTest_Success() {
        mockCourse.setId("course123");
        when(courseRepo.findById("course123")).thenReturn(Optional.of(mockCourse));

        assertDoesNotThrow(() -> courseService.deleteCourse("course123"));

        verify(courseRepo, times(1)).findById("course123");
        verify(courseRepo, times(1)).deleteById("course123");
    }

    @Test
    void deleteCourseTest_Failure() {
        when(courseRepo.findById("invalidId")).thenReturn(Optional.empty());

        Exception exception = assertThrows(ObjectDeletionException.class, () ->
                courseService.deleteCourse("invalidId"));

        assertEquals("Requested Course could not be deleted", exception.getMessage());

        verify(courseRepo, times(1)).findById("invalidId");
        verify(courseRepo, never()).deleteById(anyString());
    }
}
