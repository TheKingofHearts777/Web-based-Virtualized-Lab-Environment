package com.csproj.Cyberlab.API.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserCreateDTO {
    private final String username;
    private final String password;
    private final UserType userType;
}
