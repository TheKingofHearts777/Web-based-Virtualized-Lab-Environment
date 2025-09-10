package com.csproj.Cyberlab.API.exceptions;

import org.springframework.http.HttpStatus;

//----------------------------------------------------------------------------------------------
// Custom Exception for unexpected errors that cannot be directly resolved
//----------------------------------------------------------------------------------------------
public class InternalServerException extends RuntimeException {
    private static final HttpStatus HTTP_CODE = HttpStatus.INTERNAL_SERVER_ERROR;
    private static final String DEFAULT_MSG = "Encountered an unexpected error while processing request";

    public InternalServerException() {
        super(DEFAULT_MSG);
    }

    public InternalServerException(String msg) {
        super(msg);
    }

    public int getHttpCode() {
        return HTTP_CODE.value();
    }
}
