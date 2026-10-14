package com.careercompass.config;

import com.careercompass.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173,http://localhost:3000,https://career-compass-ahg3w7rcv-coders-85c1.vercel.app}") String allowedOrigins) {

        CorsConfiguration configuration = new CorsConfiguration();

        List<String> origins = new ArrayList<>();
        // Explicitly ensure the deployed Vercel production origin is permitted
        origins.add("https://career-compass-ahg3w7rcv-coders-85c1.vercel.app");

        // Parse any additional allowed origins from configuration or environment variable
        if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            Arrays.stream(allowedOrigins.split(","))
                    .map(String::trim)
                    .filter(origin -> !origin.isEmpty())
                    .forEach(origin -> {
                        if (!origins.contains(origin)) {
                            origins.add(origin);
                        }
                    });
        }

        configuration.setAllowedOrigins(origins);

        // Safely allow Vercel preview deployments for this project
        configuration.setAllowedOriginPatterns(List.of(
                "https://career-compass-*-coders-85c1.vercel.app"
        ));

        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        configuration.setExposedHeaders(List.of(
                "Authorization",
                "Content-Type"
        ));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtFilter,
            CorsConfigurationSource corsConfigurationSource) throws Exception {

        return http
                // CSRF disabled: Justified for stateless REST API using JWT Bearer tokens in Authorization headers without session cookies
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Permit preflight OPTIONS requests for all endpoints
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Public authentication endpoints (support /api/auth/**, /auth/**, /api/login, /login, /api/register, /register)
                        .requestMatchers(
                                "/api/auth/**", "/auth/**",
                                "/api/login", "/login",
                                "/api/register", "/register"
                        ).permitAll()
                        // Admin-only endpoints
                        .requestMatchers("/api/admin/**", "/admin/**").hasRole("ADMIN")
                        // Saved jobs require authentication
                        .requestMatchers("/api/jobs/saved", "/jobs/saved").authenticated()
                        // Public job search & browsing
                        .requestMatchers(HttpMethod.GET, "/api/jobs", "/api/jobs/**", "/jobs", "/jobs/**").permitAll()
                        // Job mutations require ADMIN role
                        .requestMatchers(HttpMethod.POST, "/api/jobs", "/jobs").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/jobs/**", "/jobs/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/jobs/**", "/jobs/**").hasRole("ADMIN")
                        // All other API endpoints require authentication
                        .anyRequest().authenticated())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
