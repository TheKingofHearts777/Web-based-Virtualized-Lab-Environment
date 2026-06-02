package com.csproj.Cyberlab.API.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

//----------------------------------------------
// Provide mappings for clients to authenticate
//----------------------------------------------
@RestController
@Slf4j
@RequestMapping("/auth")
@Tag(name = "Authentication", description = """
        **Whitelisted (non-authenticated) endpoint matchers:**
        - /auth/login
        - /auth/refresh
        - /swagger-ui/index.html
        
        **Authenticated Methods Procedure**
        1) Obtain an access token via /auth/login authentication endpoint
        2) Attach access tokens to authenticated requests: `Authorization: Bearer <token>`
        3) Upon session expiration, request a new access token via /auth/refresh
        4) If no refreshToken is supplied or refreshToken has expired, re-authenticate
        5) Invalidate refresh tokens via /auth/logout
        
        **Authorized UserTypes**
        - All users are authorized to request auth endpoints.
        """)
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /**
     * Authenticates a user
     *
     * @param dto User authentication data
     * @return User session data
     */
    @PostMapping("/login")
    @Operation(summary = "Login a user", description = """
            Validates a user's login credentials and generates session and refresh tokens.
            Session tokens must accompany all authenticated API requests: `Authorization: Bearer <token>`. Session tokens will expire every 20 minutes.
            Refresh tokens can be used to generate new session tokens when not expired.
            Refresh tokens will expire every 24h or invalidate when a user logs out.
            Re-authenticating will overwrite / invalidate any outstanding refresh token.
            
            It is recommended to store refresh tokens as a cookie so they may persist client-side between sessions.
            """)
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthDTO dto) {
        log.info("Received request to login");
        log.trace("Received request to login username: " + dto.getUsername());

        AuthResponseDTO res = authService.authenticate(dto);

        log.info("Fulfilled request to login username: " + dto.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    /**
     * Generates a new access token if supplied with a valid refresh token.
     *
     * @param token Refresh token
     * @param userId ID of User to generate new token for
     * @return New access token
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh session by requesting a new access token via a valid refresh token", description = """
            Validates the supplied refresh token and generates a new access token.
            If the refresh token is expired, re-authentication is required.
            """)
    public ResponseEntity<String> refresh(@RequestParam String token, @RequestParam String userId) {
        log.info("Received request to access refresh");

        String res = authService.refresh(token, userId);

        log.info("Fulfilled request to refresh access");

        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout a User", description = """
            A user is logged out by invalidating their persisted refresh token.
            Access tokens cannot be revoked due to their stateless nature.
            It is recommended to clear all tokens from client-side cache after a logout request.
            """)
    public ResponseEntity<Void> logout(@RequestParam String userId) {
        log.info("Received request to logout user with id: " + userId);

        authService.logout(userId);

        log.info("Resolved request to logout.");

        return ResponseEntity.noContent().build();
    }

    /**
     * Issues a client response for failed authentication and generates a corresponding log
     *
     * @param ex Authentication exception
     * @return Error http response
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Failed login attempt: " + ex.getMessage());

        HttpStatus s = (ex instanceof BadCredentialsException)
                ? HttpStatus.UNAUTHORIZED
                : HttpStatus.FORBIDDEN;

        return ResponseEntity.status(s).body("Incorrect username or password");
    }
}
