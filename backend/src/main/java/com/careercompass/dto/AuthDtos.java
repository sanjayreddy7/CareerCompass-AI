package com.careercompass.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}
    public record RegisterRequest(@NotBlank @Size(max = 100) String name, @NotBlank @Email String email,
                                  @NotBlank @Size(min = 8, max = 100) String password) {
        public RegisterRequest {
            if (email != null) email = email.trim();
        }
    }
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
        public LoginRequest {
            if (email != null) email = email.trim();
        }
    }
    public record AuthResponse(String token, UserProfile user) {}
    public record UserProfile(String id, String name, String email, String education, Integer experience,
                              String location, String preferredRole, java.util.Set<String> skills, String role) {}
    public record ProfileUpdate(@NotBlank @Size(max = 100) String name, String education,
                                @jakarta.validation.constraints.PositiveOrZero Integer experience,
                                String location, String preferredRole, java.util.Set<@NotBlank String> skills) {}
}
