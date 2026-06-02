package com.csproj.Cyberlab.API.exceptions;

//----------------------------------------------------------------------------------------------
// Custom Exception for when a resource requested under a particular ID cannot be found
//----------------------------------------------------------------------------------------------

public class FileUploadException extends RuntimeException {
    public FileUploadException(String message) {
        super(message);
    }
}
