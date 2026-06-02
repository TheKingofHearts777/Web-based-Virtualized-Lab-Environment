package com.csproj.Cyberlab.API.labTemplate;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//-------------------------------------------------------
// An entity to represent LabTemplate Mongo Documents
//-------------------------------------------------------

@Document(collection = "Labs")
@Getter
@Setter
public class LabTemplate {

    // need explicit constructor to allow null questions/objectives/vmTemplateIds since DB returns null fields when they are empty
    public LabTemplate(String name, String description, Map<String, LabQuestion> questions, List<String> objectives, List<String> vmTemplateIds) {
        this.name = name;
        this.description = description;
        this.questions = questions;
        this.objectives = objectives;
        this.vmTemplateIds = vmTemplateIds;
    }

    @Id
    private String id;

    @NonNull
    private String name;

    @NonNull
    private String description;

    private Map<String, LabQuestion> questions;

    private List<String> objectives;

    private List<String> vmTemplateIds;

    public Map<String, LabQuestion> getQuestions() {
        return (questions == null) ? new HashMap<>() : questions;
    }

    public List<String> getObjectives() {
        return (objectives == null) ? new ArrayList<>() : objectives;
    }

    public List<String> getVmTemplateIds() {
        return (vmTemplateIds == null) ? new ArrayList<>() : vmTemplateIds;
    }
}
