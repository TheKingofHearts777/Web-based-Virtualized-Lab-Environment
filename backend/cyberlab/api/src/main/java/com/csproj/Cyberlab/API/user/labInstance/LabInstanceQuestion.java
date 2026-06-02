package com.csproj.Cyberlab.API.user.labInstance;

import com.csproj.Cyberlab.API.labTemplate.LabQuestion;
import com.csproj.Cyberlab.API.labTemplate.QuestionType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document
@Data
@NoArgsConstructor // explicit constructor for Jackson since we define a custom constructor
public class LabInstanceQuestion {
    public LabInstanceQuestion(LabQuestion question) {
        this.questionNumber = question.getQuestionNumber();
        this.questionType = question.getQuestionType();
        this.question = new HashMap<>();

        for (String key : question.getQuestion().keySet()) {
            this.question.put(key, question.getQuestion().get(key));
        }
    }

    @NonNull
    private int questionNumber;

    @NonNull
    private QuestionType questionType;

    @NonNull
    private Map<String, List<String>> question;
}
