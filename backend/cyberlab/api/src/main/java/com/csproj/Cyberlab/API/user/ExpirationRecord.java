package com.csproj.Cyberlab.API.user;

import com.csproj.Cyberlab.API.user.labInstance.LabInstance;
import lombok.Data;

import java.util.List;

//---------------------------------------------------------------
// Encapsulates a User and their expired labs for deletion
//---------------------------------------------------------------
@Data
public class ExpirationRecord {
    private final String userId;
    private final List<LabInstance> labs;
}
