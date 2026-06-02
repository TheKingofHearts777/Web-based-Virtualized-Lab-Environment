package com.csproj.Cyberlab.API.exceptions;

//----------------------------------------------------------------------------------------------
// Custom Exception for when a resource cannot be deleted due to
//----------------------------------------------------------------------------------------------

public class ObjectDeletionException extends RuntimeException {
    private static final String DEFAULT_MSG = "Failed to delete object";

    public ObjectDeletionException() {
        super(DEFAULT_MSG);
    }

    public ObjectDeletionException(String message, Exception cause) {
        super(message, cause);
    }

    public ObjectDeletionException(String message) {
        super(message);
    }
}
