package com.csproj.Cyberlab.API.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

//-----------------------------------------------------------------------------------
// Constructs appropriate client responses to expected JWT access token auth errors
//-----------------------------------------------------------------------------------
@Component
@Slf4j
public class JwtExceptionHandler {
    private static final String JSON_ERR_KEY = "{\"error\": ";

    /**
     * Sets response contents for expired or malformed access tokens
     *
     * @param res HttpServletResponse
     * @param ex Exception
     * @throws IOException Error configuring HttpServletResponse
     */
    public static void handle(HttpServletResponse res, Exception ex) throws IOException {
        log.info("Handling JWT access token authentication error: " + ex);

        res.setContentType("application/json");

        if (ex instanceof ExpiredJwtException) {
            log.info("Resolved expired JWT access token");
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.getWriter().write(JSON_ERR_KEY + "\"Access token expired\"}");
        }
        else if (ex instanceof MalformedJwtException) {
            log.info("Resolved malformed JWT access token");
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write(JSON_ERR_KEY + "\"Access token malformed\"}");
        }
        else {
            log.warn("Resolved unknown access token error");
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write(JSON_ERR_KEY + "\"Access token missing or invalid\"}");
        }
    }
}
