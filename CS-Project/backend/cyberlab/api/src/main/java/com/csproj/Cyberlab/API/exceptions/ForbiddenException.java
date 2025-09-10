package com.csproj.Cyberlab.API.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

//----------------------------------------------------------------------------------------------
// Custom Exception for when a request is not allowed
//----------------------------------------------------------------------------------------------
public class ForbiddenException extends HttpException {
    public static final HttpStatus DEFAULT_HTTP_CODE = HttpStatus.FORBIDDEN;
    public static final String DEFAULT_MSG = "Request is not allowed";

    public ForbiddenException(String msg, Map<String, String> context) {
        super(DEFAULT_HTTP_CODE, msg, context);
    }

    public ForbiddenException(Map<String, String> context) {
        super(DEFAULT_HTTP_CODE, DEFAULT_MSG, context);
    }
}
