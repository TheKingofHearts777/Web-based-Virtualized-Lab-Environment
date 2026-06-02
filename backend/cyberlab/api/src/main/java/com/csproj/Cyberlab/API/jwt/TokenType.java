package com.csproj.Cyberlab.API.jwt;


//---------------------------------------------------------------
// Used to identify the type of JWT
//---------------------------------------------------------------
public enum TokenType {
    ACCESS,
    REFRESH;

    public long getExpirationMillis() {
        return switch (this) {
            case ACCESS -> 1000L * 60L * 20L;        // 20min
            case REFRESH -> 1000L * 60L * 60L * 24L; // 24h
        };
    }
}
