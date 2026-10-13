package com.careercompass.service;

import com.careercompass.dto.AuthDtos;
import com.careercompass.entity.AppUser;
import com.careercompass.repository.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.Locale;

@Service
public class UserService {
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    public UserService(AppUserRepository users, PasswordEncoder passwordEncoder) { this.users = users; this.passwordEncoder = passwordEncoder; }
    public AppUser register(AuthDtos.RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (users.existsByEmailIgnoreCase(email)) throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        return users.save(new AppUser(request.name().trim(), email, passwordEncoder.encode(request.password())));
    }
    public AppUser authenticate(AuthDtos.LoginRequest request) {
        AppUser user = users.findByEmailIgnoreCase(normalizeEmail(request.email())).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        return user;
    }
    private String normalizeEmail(String email) { return email.trim().toLowerCase(Locale.ROOT); }
    public AppUser getByEmail(String email) { return users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")); }
    public AuthDtos.UserProfile profile(AppUser user) {
        return new AuthDtos.UserProfile(user.getId(), user.getName(), user.getEmail(), user.getEducation(), user.getExperience(), user.getLocation(), user.getPreferredRole(), user.getSkills(), user.getRole());
    }
    public AuthDtos.UserProfile update(AppUser user, AuthDtos.ProfileUpdate update) {
        user.setName(update.name().trim()); user.setEducation(update.education()); user.setExperience(update.experience());
        user.setLocation(update.location()); user.setPreferredRole(update.preferredRole()); user.setSkills(update.skills());
        return profile(users.save(user));
    }
}
