package com.careercompass.controller;

import com.careercompass.dto.AuthDtos;
import com.careercompass.entity.AppUser;
import com.careercompass.security.JwtService;
import com.careercompass.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService users;
    private final JwtService jwt;
    public AuthController(UserService users, JwtService jwt) { this.users = users; this.jwt = jwt; }
    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
    public AuthDtos.AuthResponse register(@Valid @RequestBody AuthDtos.RegisterRequest request) { return response(users.register(request)); }
    @PostMapping("/login")
    public AuthDtos.AuthResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) { return response(users.authenticate(request)); }
    private AuthDtos.AuthResponse response(AppUser user) { return new AuthDtos.AuthResponse(jwt.createToken(user.getEmail(), user.getRole()), users.profile(user)); }
}