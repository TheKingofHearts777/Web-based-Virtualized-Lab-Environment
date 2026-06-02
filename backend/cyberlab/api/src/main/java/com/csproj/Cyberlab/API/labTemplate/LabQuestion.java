package com.csproj.Cyberlab.API.labTemplate;

import lombok.Data;
import lombok.NonNull;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

//-----------------------------------------------------------------------------
// An entity to represent Question Mongo Documents. Nested inside LabTemplate
//-----------------------------------------------------------------------------
@Document
@Data
public class LabQuestion {
    @NonNull
    private int questionNumber;

    @NonNull
    private QuestionType questionType;

    @NonNull
    private Map<String, List<String>> question;

    @NonNull
    private String answer;
}
