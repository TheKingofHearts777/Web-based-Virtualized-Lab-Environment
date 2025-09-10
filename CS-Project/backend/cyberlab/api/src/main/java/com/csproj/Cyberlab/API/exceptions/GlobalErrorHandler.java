package com.csproj.Cyberlab.API.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

//----------------------------------------------------------------------------------------------
// Provide generic resolution and logging for common / expected exceptions
// * Resource specific handling should be implemented that resource package or Controller
//----------------------------------------------------------------------------------------------
@ControllerAdvice
@Slf4j
public class GlobalErrorHandler {

    /**
     * Handle basic expected CustomErrors that do not require additional logic.
     * Returns the Exception's message, code and context to the client. If none were supplied, the exceptions default
     * values are used.
     *
     * @param ex HttpException
     * @return resolved ResponseEntity for client
     */
    @ExceptionHandler(HttpException.class)
    public ResponseEntity<HttpExceptionResponse> genericHttpExceptionHandler(HttpException ex) {
        HttpExceptionResponse res = new HttpExceptionResponse(ex.getMessage(), ex.getContext());

        String logStr = "Resolved exception with: " + ex;
        log.info(logStr);

        return ResponseEntity.status(ex.getHttpCode()).body(res);
    }

    /**
     * Handle ObjectDeletionException and return an appropriate response to the client.
     * Logs the error and returns a CONFLICT (409) response with the exception's message.
     *
     * @param ex ObjectDeletionException
     * @return ResponseEntity with error message
     */
    @ExceptionHandler(ObjectDeletionException.class)
    public ResponseEntity<String> handleObjectDeletionException(ObjectDeletionException ex) {
        String logStr = "Object deletion failed: " + ex.getMessage();

        log.info(logStr);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    /**
     * Resolves InternalServerErrors by issuing a warn log and supplying the client a generic response
     * @param ex InternalServerException
     * @return ResponseEntity with error message
     */
    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<String> internalServerExceptionHandler(HttpException ex) {
        String logStr = ex.getMessage();
        log.error(logStr);

        return ResponseEntity.internalServerError().build();
    }
}
