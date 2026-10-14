package com.careercompass.controller;

import com.careercompass.dto.AuthDtos;
import com.careercompass.entity.AppUser;
import com.careercompass.security.JwtService;
import com.careercompass.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private UserService userService;
    private JwtService jwtService;
    private AuthController authController;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        jwtService = mock(JwtService.class);
        authController = new AuthController(userService, jwtService);
    }

    @Test
    void controllerHasCorrectRequestMapping() {
        RestController restAnnotation = AuthController.class.getAnnotation(RestController.class);
        assertNotNull(restAnnotation);

        RequestMapping mapping = AuthController.class.getAnnotation(RequestMapping.class);
        assertNotNull(mapping);
        assertArrayEquals(new String[]{"/api/auth", "/auth"}, mapping.value());
    }

    @Test
    void loginAuthenticatesAndReturnsTokenAndProfile() {
        AppUser mockUser = new AppUser("Alex Morgan", "demo@careercompass.dev", "hashedPass");
        mockUser.setRole("USER");

        AuthDtos.UserProfile userProfile = new AuthDtos.UserProfile(
                "user-1", "Alex Morgan", "demo@careercompass.dev",
                "B.Tech", 2, "Bengaluru", "Java Developer",
                Set.of("Java", "Spring Boot"), "USER"
        );

        when(userService.authenticate(any(AuthDtos.LoginRequest.class))).thenReturn(mockUser);
        when(jwtService.createToken(mockUser.getEmail(), mockUser.getRole())).thenReturn("mock-jwt-token");
        when(userService.profile(mockUser)).thenReturn(userProfile);

        AuthDtos.LoginRequest request = new AuthDtos.LoginRequest("demo@careercompass.dev", "DemoPass123!");
        AuthDtos.AuthResponse response = authController.login(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.token());
        assertEquals("demo@careercompass.dev", response.user().email());
        assertEquals("Alex Morgan", response.user().name());

        verify(userService).authenticate(request);
        verify(jwtService).createToken("demo@careercompass.dev", "USER");
    }

    @Test
    void registerCreatesUserAndReturnsTokenAndProfile() {
        AppUser mockUser = new AppUser("New User", "new@careercompass.dev", "hashedPass");
        mockUser.setRole("USER");

        AuthDtos.UserProfile userProfile = new AuthDtos.UserProfile(
                "user-2", "New User", "new@careercompass.dev",
                null, null, null, null, Set.of(), "USER"
        );

        when(userService.register(any(AuthDtos.RegisterRequest.class))).thenReturn(mockUser);
        when(jwtService.createToken(mockUser.getEmail(), mockUser.getRole())).thenReturn("new-jwt-token");
        when(userService.profile(mockUser)).thenReturn(userProfile);

        AuthDtos.RegisterRequest request = new AuthDtos.RegisterRequest("New User", "new@careercompass.dev", "Password123!");
        AuthDtos.AuthResponse response = authController.register(request);

        assertNotNull(response);
        assertEquals("new-jwt-token", response.token());
        assertEquals("new@careercompass.dev", response.user().email());
        assertEquals("New User", response.user().name());

        verify(userService).register(request);
    }
}
