package com.csproj.Cyberlab.API.config;

import com.csproj.Cyberlab.API.exceptions.NotFoundException;
import com.csproj.Cyberlab.API.user.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;

//---------------------------------------------------------------
// Configure AuthenticationProvider
//---------------------------------------------------------------
@Configuration
@RequiredArgsConstructor
public class AuthConfig {
    private final UserRepo userRepo;
    public static final int passwordEncodingStrength = 10;

    /**
     * Fetches AuthenticationProvider
     * Asserts usage of UserDetails for auth
     * Sets password encoder for pwd hashing
     *
     * @return AuthenticationProvider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    /**
     * Constructs a password encoder for password hashing
     * Sets custom "strength" level and adds random salt at the end of hash
     *
     * @return PasswordEncoder
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(passwordEncodingStrength, new SecureRandom());
    }

    /**
     * Single use UserDetails service to fetch users by username
     *
     * @return User if found
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found", null));
    }

    /**
     * Constructs control to manage Authentication
     * Uses Spring Security default username & password authentication
     * JWT auth is inserted before this in the filter chain to bypass if a valid JWT is provided
     *
     * @param config Authentication configuration to be used
     * @return AuthenticationManager
     * @throws Exception Any Auth related Exception
     */
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
