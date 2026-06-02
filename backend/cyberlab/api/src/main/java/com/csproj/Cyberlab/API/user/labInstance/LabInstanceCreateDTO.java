package com.csproj.Cyberlab.API.user.labInstance;

import lombok.Data;

import java.sql.Date;

//---------------------------------------------------------------
// Models a request body for a LabInstance create request
//---------------------------------------------------------------
@Data
public class LabInstanceCreateDTO {
    private String templateId;  // parent template to clone
    private String courseId;
    private Date dueDate;
}
