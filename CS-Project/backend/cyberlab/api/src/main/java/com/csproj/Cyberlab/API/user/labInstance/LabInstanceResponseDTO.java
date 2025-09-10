package com.csproj.Cyberlab.API.user.labInstance;

import com.csproj.Cyberlab.API.user.labInstance.vmInstance.VmInstance;
import com.csproj.Cyberlab.API.user.labInstance.vmInstance.VmInstanceResponseDTO;
import lombok.Data;

import java.util.*;

@Data
public class LabInstanceResponseDTO {
    private final String id;
    private final String templateName;
    private final String description;
    private final List<String> objectives;
    private final Map<String, LabInstanceQuestion> questions;
    private final String courseId;
    private final String templateId;
    private final Date dateLastAccessed;
    private final Map<String, VmInstanceResponseDTO> vmInstances;
    private final List<String> userAnswers;
    private final boolean completed;
    private final Date dueDate;

    public LabInstanceResponseDTO(LabInstance lab) {
        this.id = lab.getId();
        this.templateName = lab.getTemplateName();
        this.description = lab.getDescription();
        this.objectives = lab.getObjectives();
        this.questions = lab.getQuestions();
        this.courseId = lab.getCourseId();
        this.templateId = lab.getTemplateId();
        this.dateLastAccessed = lab.getDateLastAccessed();
        this.vmInstances = new HashMap<>();
        this.userAnswers = new ArrayList<>(lab.getUserAnswers());
        this.completed = lab.isCompleted();
        this.dueDate = lab.getDueDate();

        for (Map.Entry<String, VmInstance> entry : lab.getVmInstances().entrySet()) {
            this.vmInstances.put(entry.getKey(), new VmInstanceResponseDTO(entry.getValue()));
        }
    }
}
