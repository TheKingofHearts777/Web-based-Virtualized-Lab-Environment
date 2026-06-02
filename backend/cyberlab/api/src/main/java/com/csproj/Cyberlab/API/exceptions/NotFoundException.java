package com.csproj.Cyberlab.API.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

//----------------------------------------------------------------------------------------------
// Custom Exception for when a resource requested under a particular ID cannot be found
//----------------------------------------------------------------------------------------------
public class NotFoundException extends HttpException {
    private static final HttpStatus DEFAULT_HTTP_CODE = HttpStatus.NOT_FOUND;
    private static final String DEFAULT_MSG = "Failed to find requested resource";

    public NotFoundException(String msg, Map<String, String> context) {
        super(DEFAULT_HTTP_CODE, msg, context);
    }

    public NotFoundException(Map<String, String> context) {
        super(DEFAULT_HTTP_CODE, DEFAULT_MSG, context);
    }
}
