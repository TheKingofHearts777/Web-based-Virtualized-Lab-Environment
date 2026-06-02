package com.csproj.Cyberlab.API.course;

import com.csproj.Cyberlab.API.auth.AuthService;
import com.csproj.Cyberlab.API.exceptions.NotFoundException;
import com.csproj.Cyberlab.API.exceptions.ObjectDeletionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepo courseRepo;
    private final AuthService authService;

    /**
     * Query database for a Course by its ID
     *
     * @param id ID of requested Course
     * @return Course found
     * @throws NotFoundException no Course associated with supplied ID
     */
    public Course findById(String id) {
        Optional<Course> optC = courseRepo.findById(id);

        if (optC.isPresent()) {
            Course c = optC.get();
            List<String> aIds = c.getInstructors();
            aIds.addAll(c.getStudents());
            authService.authorizeResourceAccess(aIds);
        }

        return optC.orElseThrow(() -> new NotFoundException("Requested Course not found", null));
    }

    /**
     * Query database for a list of Course
     *
     * @param limit pageSize
     * @param offset pageNumber
     * @return Paginated sublist of Course
     */
    public List<Course> getList(int limit, int offset) {
        PageRequest pr = PageRequest.of(offset, limit);
        Page<Course> p = courseRepo.findAll(pr);

        return p.getContent();
    }

    /**
     * Create a new Course and saves to persistence layer
     * Lookup lists are initialized to null
     *
     * @param dto New Course data
     * @return Newly created Course
     */
    public Course create(CourseCreateDTO dto) {
        Course course = new Course(dto.getName());

        return courseRepo.save(course);
    }

    /**
     * Deletes a Course from database by its id
     *
     * @param id ID of course to delete
     * @throws ObjectDeletionException if requested Course cannot be found
     */
    public void deleteCourse(String id) {
        log.info(String.format("Attempting to delete Course with id: %s", id));

        String courseId;
        try {
            courseId = courseRepo.findById(id).orElseThrow().getId();
        } catch (NoSuchElementException e) {
            throw new ObjectDeletionException("Requested Course could not be deleted");
        }

        courseRepo.deleteById(courseId);
        log.info(String.format("Successfully deleted Course with id: %s", id));
    }

    /**
     * Get list of courses associated with a student ID
     *
     * @param studentId ID of student to be searched
     * @param limit Pagination maximum
     * @param offset Pagination offset
     * @return List of courses associated to user
     */
    public List<Course> getCourseListByStudent(String studentId, int limit, int offset) {
        authService.authorizeResourceAccess(studentId);
        List<Course> temp = courseRepo.findByStudentId(studentId);

        if(temp.isEmpty()) {
            throw new NotFoundException("User not associated with any courses", null);
        }

        int endIndex = offset + limit;
        if (endIndex + offset > temp.size())
        {
            endIndex = temp.size() - offset;
        }

        return temp.subList(offset, endIndex);
    }

    /**
     * Get list of courses associated with a teacher ID
     *
     * @param teacherId ID of teacher to be searched
     * @param limit Pagination maximum
     * @param offset Pagination offset
     * @return List of courses associated to user
     */
    public List<Course> getCourseListByTeacher(String teacherId, int limit, int offset) {
        authService.authorizeResourceAccess(teacherId);
        List<Course> temp = courseRepo.findByTeacherId(teacherId);

        if (temp.isEmpty()) {
            throw new NotFoundException("User not associated with any courses", null);
        }

        int endIndex = offset + limit;
        if (endIndex + offset > temp.size()) {
            endIndex = temp.size() - offset;
        }

        return temp.subList(offset, endIndex);
    }
}
