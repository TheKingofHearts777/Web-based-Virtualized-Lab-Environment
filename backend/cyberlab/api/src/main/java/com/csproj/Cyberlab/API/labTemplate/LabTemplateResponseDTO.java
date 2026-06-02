package com.csproj.Cyberlab.API.labTemplate;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class LabTemplateResponseDTO {
    private final String id;
    private final String name;
    private final String description;
    private final Map<String, LabQuestionResponseDTO> questions;
    private final List<String> objectives;
    private final List<String> vmTemplateIds;

    public LabTemplateResponseDTO(LabTemplate lab) {
        this.id = lab.getId();
        this.name = lab.getName();
        this.description = lab.getDescription();
        this.questions = new HashMap<>();
        this.objectives = new ArrayList<>(lab.getObjectives());
        this.vmTemplateIds = new ArrayList<>(lab.getVmTemplateIds());

        for (Map.Entry<String, LabQuestion> entry : lab.getQuestions().entrySet()) {
            this.questions.put(entry.getKey(), new LabQuestionResponseDTO(entry.getValue()));
        }
    }
}
