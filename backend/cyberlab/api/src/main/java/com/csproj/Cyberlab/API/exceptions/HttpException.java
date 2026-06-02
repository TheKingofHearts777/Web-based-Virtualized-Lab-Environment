package com.csproj.Cyberlab.API.exceptions;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import java.util.Map;

//------------------------------------------------------------------------------------------------------
// Custom Exception wrapper enables associating exceptions with http status codes and exception context
// * used by error resolvers to provide appropriate client responses and logging
//------------------------------------------------------------------------------------------------------
@ToString
public class HttpException extends RuntimeException {
    private final HttpStatus httpCode;
    @Getter private final Map<String, String> context;

    HttpException(HttpStatus httpCode, String msg, Map<String, String> context) {
        super(msg);
        this.httpCode = httpCode;
        this.context = context;
    }

    public int getHttpCode() {
        return httpCode.value();
    }
}

/*----------------------------------------------------------------------------------------------
 Implement custom Http Exceptions with:

public class CustomException extends HttpException {
    public static final HttpStatus DEFAULT_HTTP_CODE = HttpStatus.EXAMPLE;
    public static final String DEFAULT_MSG = "Default err msg here";

    public CustomException(String msg, Map<String, String> context) {
        super(DEFAULT_HTTP_CODE, msg, context);
    }

    public CustomException(Map<String, String> context) {
        super(DEFAULT_HTTP_CODE, DEFAULT_MSG, context);
    }
}
---------------------------------------------------------------------------------------------*/