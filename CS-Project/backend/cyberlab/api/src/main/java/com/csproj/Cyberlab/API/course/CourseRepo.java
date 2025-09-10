package com.csproj.Cyberlab.API.course;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface CourseRepo extends MongoRepository<Course, String> {
    @Query("{ ?0 { $in: 'students' } }")
    List<Course> findByStudentId(String studentId);

    @Query("{ ?0 { $in: 'instructors' } }")
    List<Course> findByTeacherId(String teacherId);
}
