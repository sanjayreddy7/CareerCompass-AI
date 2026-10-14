package com.careercompass.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SecurityCorsTest {

    @Test
    void corsConfigurationAllowsDeployedVercelOriginAndPreviewOrigins() {
        SecurityConfig config = new SecurityConfig();
        CorsConfigurationSource source = config.corsConfigurationSource(
                "http://localhost:5173,http://127.0.0.1:5173,https://career-compass-ahg3w7rcv-coders-85c1.vercel.app"
        );

        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/auth/login");
        request.addHeader("Origin", "https://career-compass-ahg3w7rcv-coders-85c1.vercel.app");
        request.addHeader("Access-Control-Request-Method", "POST");
        request.addHeader("Access-Control-Request-Headers", "Content-Type");

        CorsConfiguration corsConfig = source.getCorsConfiguration(request);
        assertNotNull(corsConfig);

        String allowedOrigin = corsConfig.checkOrigin("https://career-compass-ahg3w7rcv-coders-85c1.vercel.app");
        assertEquals("https://career-compass-ahg3w7rcv-coders-85c1.vercel.app", allowedOrigin);

        // Test preview origin matching pattern
        String previewOrigin = corsConfig.checkOrigin("https://career-compass-preview123-coders-85c1.vercel.app");
        assertEquals("https://career-compass-preview123-coders-85c1.vercel.app", previewOrigin);

        // Test localhost
        String localhostOrigin = corsConfig.checkOrigin("http://localhost:5173");
        assertEquals("http://localhost:5173", localhostOrigin);

        // Verify methods
        assertTrue(corsConfig.getAllowedMethods().containsAll(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")));

        // Verify headers
        assertTrue(corsConfig.getAllowedHeaders().containsAll(List.of("Authorization", "Content-Type")));

        // Test non-api prefixed path /auth/register
        MockHttpServletRequest nonApiRequest = new MockHttpServletRequest("OPTIONS", "/auth/register");
        nonApiRequest.addHeader("Origin", "https://career-compass-ahg3w7rcv-coders-85c1.vercel.app");
        CorsConfiguration nonApiCors = source.getCorsConfiguration(nonApiRequest);
        assertNotNull(nonApiCors);
        assertEquals("https://career-compass-ahg3w7rcv-coders-85c1.vercel.app", nonApiCors.checkOrigin("https://career-compass-ahg3w7rcv-coders-85c1.vercel.app"));
    }
}
