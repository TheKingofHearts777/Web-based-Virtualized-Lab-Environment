package com.csproj.Cyberlab.API.auth;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

//---------------------------------------------------
// Represents data used in an authentication request
//---------------------------------------------------
@RequiredArgsConstructor
@Getter
public class AuthDTO {

    @NonNull
    private String username;

    @NonNull
    private String password;
}
