package com.csproj.Cyberlab.API.user.labInstance;

import java.util.List;

//---------------------------------------------------------------
// Models a request body for a LabInstance update request
//---------------------------------------------------------------
public record LabInstanceUpdateDTO(
        List<String> userAnswers
) {}
