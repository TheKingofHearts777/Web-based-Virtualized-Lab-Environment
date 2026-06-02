package com.csproj.Cyberlab.API.user;

import com.csproj.Cyberlab.API.user.labInstance.LabInstance;
import com.csproj.Cyberlab.API.user.labInstance.LabInstanceResponseDTO;
import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
public class UserResponseDTO {
    private final String id;
    private final Date lastTimeVisited;
    private final String username;
    private final UserType userType;
    private final Map<String, LabInstanceResponseDTO> labInstances;

    public UserResponseDTO(User user) {
        this.id = user.getId();
        this.lastTimeVisited = user.getLastTimeVisited();
        this.username = user.getUsername();
        this.userType = user.getUserType();
        this.labInstances = new HashMap<>();

        for (Map.Entry<String, LabInstance> entry : user.getLabInstances().entrySet()) {
            labInstances.put(entry.getKey(), new LabInstanceResponseDTO(entry.getValue()));
        }
    }
}
