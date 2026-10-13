package com.careercompass.controller;

import com.careercompass.dto.AuthDtos;
import com.careercompass.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService users;
    public UserController(UserService users) { this.users = users; }
    @GetMapping("/profile") public AuthDtos.UserProfile profile(Authentication auth) { return users.profile(users.getByEmail(auth.getName())); }
    @PutMapping("/profile") public AuthDtos.UserProfile update(Authentication auth, @Valid @RequestBody AuthDtos.ProfileUpdate update) { return users.update(users.getByEmail(auth.getName()), update); }
}