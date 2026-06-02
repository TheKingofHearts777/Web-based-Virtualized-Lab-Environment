package com.csproj.Cyberlab.API.exceptions;

import java.util.Map;

public record HttpExceptionResponse(String message, Map<String, String> context) {}
