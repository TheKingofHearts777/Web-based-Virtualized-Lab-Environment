package com.csproj.Cyberlab.API.course;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

//-------------------------------------------------------
// Provide mappings for clients to interact with Courses
//-------------------------------------------------------
@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "Courses serve as lookup tables for labs that users instruct or are enrolled in.")
@Slf4j
public class CourseController {
    private final CourseService courseService;

    /**
     * Get a Course ID
     *
     * @param id ID of the requested Course
     * @return Course associated with the request ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a Course by ID", description = """
            **Authorized UserTypes:**
            - Admin
            - Instructor
            - Student
            
            **Resource Level Authorization**
            - Students may only request Courses they are enrolled in.
            """)
    @PreAuthorize("(hasRole('ROLE_ADMIN')) or (hasRole('ROLE_INSTRUCTOR')) or (hasRole(ROLE_STUDENT'))")
    public ResponseEntity<CourseResponseDTO> findById(@RequestParam String id) {
        log.info("Received request to GET Course by ID");
        log.trace("Received request to GET Course: courses/" + id);

        Course course = courseService.findById(id);
        CourseResponseDTO res = new CourseResponseDTO(course);

        log.trace(String.format("Fulfilled request to get Course with (course = %s)", res));

        return ResponseEntity.ok().body(res);
    }

    /**
     * Get a list of courses associated with a student ID
     *
     * @return paginated list of courses
     */
    @GetMapping("/by-student-id")
    @Operation(summary = "Get a paginated list of Courses a Student is enrolled in", description = """
            **Authorized UserTypes:**
            - Admin
            - Instructor
            - Student
            
            **Role Based Authorization:**
            - Students can only request their own enrolled courses
            """)
    public ResponseEntity<List<Course>>  findByUserIdByStudent(@RequestParam String studentId, @RequestParam int limit, @RequestParam int offset) {
        log.info("Received request to GET Course by Student ID");
        log.trace("Received request to GET Course by Student ID: courses/by-student-id" + studentId);

        List<Course> courseList = courseService.getCourseListByStudent(studentId, limit, offset);

        log.trace(String.format("Fulfilled Course list request by student with (listSize = %s", courseList.size()));

        return ResponseEntity.ok().body(courseList);
    }

    /**
     * Get a list of courses associated with a teacher ID
     *
     * @return paginated list of courses
     */
    @GetMapping("/by-teacher-id")
    @Operation(summary = "Get a paginated list of Courses taught by an Instructor.", description = """
            **Authorized UserTypes:**
            - Admin
            - Instructor
            
            **Role based Authorization:**
            - Instuctors can only request courses they teach
            """)
    @PreAuthorize("(hasRole('ROLE_ADMIN')) OR (hasRole('ROLE_INSTRUCTOR')")
    public ResponseEntity<List<Course>>  findByUserIdByTeacher(@RequestParam String teacherId, @RequestParam int limit, @RequestParam int offset) {
        log.info("Received request to GET Course by Teacher ID");
        log.trace("Received request to GET Course by Teacher ID: courses/by-teacher-id" + teacherId);

        List<Course> courseList = courseService.getCourseListByTeacher(teacherId, limit, offset);

        log.trace(String.format("Fulfilled Course list request by teacher with (listSize = %s", courseList.size()));

        return ResponseEntity.ok().body(courseList);
    }

    /**
     * Get a paginated list of Courses
     *
     * @param limit Number of Courses per page
     * @param offset Page number
     * @return Paginated sublist of Courses
     */
    @GetMapping
    @Operation(summary = "Get a paginated list from all Courses.", description = """
            **Authorized UserTypes:**
            - Admin
            
            **Instructor & User access:**
            - See `/courses/by-student-id` and `/courses/by-teacher-id`
            """)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<CourseResponseDTO>> getList(@RequestParam int limit, @RequestParam int offset) {
        log.info("Received request to GET Course list");

        List<Course> courses = courseService.getList(limit, offset);
        List<CourseResponseDTO> res = new ArrayList<>(
                courses.stream().map(CourseResponseDTO::new).toList()
        );

        log.trace(String.format("Fulfilled GET Course list with (listSize = %s)", courses.size()));

        return ResponseEntity.ok().body(res);
    }

    @PostMapping
    @Operation(summary = "Creates a new Course", description = """
            **Authorized UserTypes:**
            - Admin
            
            **Note:**
            - All course fields except name are initialized to null and must be assigned via subsequent update requests.
            """)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<CourseResponseDTO> create(@RequestBody CourseCreateDTO dto, UriComponentsBuilder ucb) {
        log.info("Received request to CREATE Course");
        log.trace("Received request to CREATE Course: " + dto);

        Course course = courseService.create(dto);
        CourseResponseDTO res = new CourseResponseDTO(course);

        URI uri = ucb
                .path("/{id}")
                .buildAndExpand(course.getId())
                .toUri();

        log.trace(String.format("Fulfilled CREATE Course with (course = %s)", res));

        return ResponseEntity.created(uri).body(res);
    }
}
