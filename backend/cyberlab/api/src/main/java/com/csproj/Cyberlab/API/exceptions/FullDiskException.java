package com.csproj.Cyberlab.API.exceptions;

import org.springframework.http.HttpStatus;

//--------------------------------------------------------------------------------------------
// Custom exception for when the disk that would be used for an operation is full
//--------------------------------------------------------------------------------------------
public class FullDiskException extends RuntimeException {

    private static final HttpStatus HTTP_CODE = HttpStatus.INSUFFICIENT_STORAGE;
    private static final String MSG = "Insufficient storage available for creation";

    public FullDiskException() {
        super(MSG);
    }

    public FullDiskException(String message) {
        super(message);
    }
}
