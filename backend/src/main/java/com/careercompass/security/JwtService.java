package com.careercompass.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;

    public JwtService(@Value("${app.jwt-secret:careercompass-default-secret-key-must-be-at-least-32-bytes!}") String secret) {
        if (secret == null || secret.isBlank()) {
            secret = "careercompass-default-secret-key-must-be-at-least-32-bytes!";
        }
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret must contain at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
    }

    public String createToken(String email, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(86400)))
                .signWith(key)
                .compact();
    }

    public String subject(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }
}
