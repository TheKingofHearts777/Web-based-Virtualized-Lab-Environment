package com.csproj.Cyberlab.API.course;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CourseResponseDTO {
    private final String id;
    private final String name;
    private final List<String> instructors;
    private final List<String> students;
    private final List<String> labTemplates;

    public CourseResponseDTO(Course course) {
        this.id = course.getId();
        this.name = course.getName();
        this.instructors = new ArrayList<>(course.getInstructors());
        this.students = new ArrayList<>(course.getStudents());
        this.labTemplates = new ArrayList<>(course.getLabTemplates());
    }
}
