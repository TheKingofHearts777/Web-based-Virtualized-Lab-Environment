package com.csproj.Cyberlab.API.user;

import com.csproj.Cyberlab.API.user.labInstance.LabInstance;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Document(collection = "Users")
@Data
public class User implements UserDetails {
    @Id
    private String id;

    private Date lastTimeVisited;

    @NonNull
    private String username;

    @NonNull
    private String password;

    @NonNull
    private UserType userType;

    private Map<String, LabInstance> labInstances;

    private String refreshToken;

    public Map<String, LabInstance> getLabInstances() {
        return (labInstances == null) ? new HashMap<>() : labInstances;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + userType.name().toUpperCase()));
    }
}
