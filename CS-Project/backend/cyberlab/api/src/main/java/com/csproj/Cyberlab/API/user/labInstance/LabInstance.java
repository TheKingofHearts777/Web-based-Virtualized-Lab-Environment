package com.csproj.Cyberlab.API.user.labInstance;

import com.csproj.Cyberlab.API.user.labInstance.vmInstance.VmInstance;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.*;

//-------------------------------------------------------
// An entity to represent LabInstance Mongo Documents
//-------------------------------------------------------
@Data
public class LabInstance {

    public LabInstance(String templateName, String description, List<String> objectives,
                       Map<String, LabInstanceQuestion> questions, String courseId, String templateId,
                       Date dateLastAccessed, Map<String, VmInstance> vmInstances, List<String> userAnswers,
                       boolean completed, Date dueDate)
    {
        this.id = new ObjectId().toString();
        this.templateName = templateName;
        this.description = description;
        this.objectives = objectives;
        this.questions = questions;
        this.courseId = courseId;
        this.templateId = templateId;
        this.dateLastAccessed = dateLastAccessed;
        this.vmInstances = vmInstances;
        this.userAnswers = userAnswers;
        this.completed = completed;
        this.dueDate = dueDate;
    }

    @Id
    private String id;

    @NonNull
    private String templateName;

    @NonNull
    private String description;

    private List<String> objectives;

    private Map<String, LabInstanceQuestion> questions;

    @NonNull
    @Field("courseID")
    private String courseId;

    @NonNull
    private String templateId;

    @NonNull
    private Date dateLastAccessed;

    @NonNull
    private Map<String, VmInstance> vmInstances;

    @NonNull
    private List<String> userAnswers;

    @NonNull
    private boolean completed;

    @NonNull
    private Date dueDate;

    public Map<String, LabInstanceQuestion> getQuestions() {
        return (questions == null) ? new HashMap<>() : questions;
    }

    public List<String> getObjectives() {
        return (objectives == null) ? new ArrayList<>() : objectives;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LabInstance)) return false;
        if (o == this) return true;

        return ((LabInstance) o).getId().equals(this.id);
    }
}
