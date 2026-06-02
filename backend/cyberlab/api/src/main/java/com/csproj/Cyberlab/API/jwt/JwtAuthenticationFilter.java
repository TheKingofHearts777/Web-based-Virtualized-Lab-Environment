package com.csproj.Cyberlab.API.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

//------------------------------------------------------------------------------------
// Security preprocess filter to enforce presence and validity of JWT access tokens
//------------------------------------------------------------------------------------
@Configuration
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String AUTH_HEADER = "Authorization";
    private static final String AUTH_PREFIX = "Bearer ";
    private static final int AUTH_PREFIX_LEN = 7;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Defines authentication process using JWTs
     * Continues to the next filter if JWT is missing or invalid. Otherwise, the token is verified and completes
     * security filtering. If JWT is invalid, it will attempt to create a new one and attach it to the security context.
     *
     * @param request HTTP request - expected to contain JWT
     * @param response HTTP response - do not modify
     * @param filterChain SecurityFilterChain
     * @throws ServletException Servlet Error
     * @throws IOException IO Error
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader(AUTH_HEADER);

        // JWT is missing or invalid - continue to next filter
        if (authHeader == null || !authHeader.startsWith(AUTH_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(AUTH_PREFIX_LEN);

        try {
            String username = jwtService.extractUsername(jwt);

            if (needsAuthentication(username)) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }
        catch (Exception e) {
            JwtExceptionHandler.handle(response, e);
            return; // terminate filtering if JWT was invalid
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Determines if a user requires authentication
     *
     * @param username User's username
     * @return true if authentication is needed
     */
    private boolean needsAuthentication(String username) {
        return username != null && SecurityContextHolder.getContext().getAuthentication() == null;
    }
}
