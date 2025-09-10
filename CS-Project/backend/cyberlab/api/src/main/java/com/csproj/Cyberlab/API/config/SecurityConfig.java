package com.csproj.Cyberlab.API.config;

import com.csproj.Cyberlab.API.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

//---------------------------------------------------------------
// Configure API security protocols and standards
//---------------------------------------------------------------
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authProvider;
    private final CorsConfigurationSource corsConfigurationSource;

    /**
     * Configures API SecurityFilterChain
     * Disables CSRF, secures all endpoints except a few whitelisted ones
     * Sets statelss policy & inserts jwtAuthFilter into SecurityFilterChain
     *
     * @param http Http settings
     * @return SecurityFilterChain
     * @throws Exception Generic configuration error
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer
                        .configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(req -> {
                    req.requestMatchers("/auth/login").permitAll();
                    req.requestMatchers("/auth/refresh").permitAll();
                    req.requestMatchers("/swagger-ui/**").permitAll();
                    req.requestMatchers("/v3/api-docs*/**").permitAll();
                    req.anyRequest().authenticated();
                })
                // JWT auth is stateless
                .sessionManagement(s -> s
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // JWT Auth supersedes Spring's builtin standard auth
                .authenticationProvider(authProvider)
                    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}