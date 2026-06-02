package com.csproj.Cyberlab.API.course;

import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "Classes")
@Data
public class Course {
    @Id
    private String id;

    @NonNull
    private String name;

    private List<String> instructors;

    private List<String> students;

    private List<String> labTemplates;

    public List<String> getInstructors() {
        return (instructors == null) ? new ArrayList<>() : instructors;
    }

    public List<String> getStudents() {
        return (students == null) ? new ArrayList<>() : students;
    }

    public List<String> getLabTemplates() {
        return (labTemplates == null) ? new ArrayList<>() : labTemplates;
    }
}
