package com.csproj.Cyberlab.API.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

//----------------------------------------------------------------------------------------------
// Custom Exception for when a resource with invalid or missing arguments is supplied
//----------------------------------------------------------------------------------------------
public class BadRequestException extends HttpException {
    public static final HttpStatus DEFAULT_HTTP_CODE = HttpStatus.BAD_REQUEST;
    public static final String DEFAULT_MSG = "Request contains invalid or missing arguments";

    public BadRequestException(String msg, Map<String, String> context) {
        super(DEFAULT_HTTP_CODE, msg, context);
    }

    public BadRequestException(Map<String, String> context) {
        super(DEFAULT_HTTP_CODE, DEFAULT_MSG, context);
    }
}
