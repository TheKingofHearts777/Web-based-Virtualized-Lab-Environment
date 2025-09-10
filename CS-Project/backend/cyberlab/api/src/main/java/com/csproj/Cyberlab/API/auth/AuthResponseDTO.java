package com.csproj.Cyberlab.API.auth;

import com.csproj.Cyberlab.API.user.UserResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

//----------------------------------------------------
// Represents data used in an authentication response
//----------------------------------------------------
@AllArgsConstructor
@Getter
public class AuthResponseDTO {
    private String accessToken;
    private String refreshToken;
    private UserResponseDTO user;
}
