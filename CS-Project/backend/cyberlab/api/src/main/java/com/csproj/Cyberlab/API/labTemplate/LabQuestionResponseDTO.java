package com.csproj.Cyberlab.API.labTemplate;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class LabQuestionResponseDTO {
    private final int questionNumber;
    private final QuestionType questionType;
    private final Map<String, List<String>> question;
    private final String answer;

    public LabQuestionResponseDTO(LabQuestion question) {
        this.questionNumber = question.getQuestionNumber();
        this.questionType = question.getQuestionType();
        this.question = new HashMap<>();
        this.answer = question.getAnswer();

        for (Map.Entry<String, List<String>> entry : question.getQuestion().entrySet()) {
            this.question.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
    }
}
