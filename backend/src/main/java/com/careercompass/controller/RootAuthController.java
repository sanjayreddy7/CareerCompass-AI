package com.careercompass.controller;

import com.careercompass.dto.AuthDtos;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootAuthController {
    private final AuthController authController;

    public RootAuthController(AuthController authController) {
        this.authController = authController;
    }

    @PostMapping({"/register", "/api/register"})
    @ResponseStatus(HttpStatus.CREATED)
    public AuthDtos.AuthResponse register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        return authController.register(request);
    }

    @PostMapping({"/login", "/api/login"})
    public AuthDtos.AuthResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return authController.login(request);
    }
}
