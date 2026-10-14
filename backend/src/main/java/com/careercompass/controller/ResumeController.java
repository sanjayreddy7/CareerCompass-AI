package com.careercompass.controller;

import com.careercompass.dto.JobDtos;
import com.careercompass.service.ResumeService;
import com.careercompass.service.UserService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {
    private final ResumeService resumes;
    private final UserService users;
    private final JdbcTemplate jdbc;

    public ResumeController(ResumeService resumes, UserService users, @Autowired(required = false) JdbcTemplate jdbc) {
        this.resumes = resumes;
        this.users = users;
        this.jdbc = jdbc;
    }

    @PostConstruct
    public void init() {
        ensureTable();
    }

    private void ensureTable() {
        if (jdbc != null) {
            try {
                jdbc.execute("CREATE TABLE IF NOT EXISTS resume_extracted_skills (" +
                        "resume_id VARCHAR(36) NOT NULL, " +
                        "skill VARCHAR(100) NOT NULL" +
                        ")");
            } catch (Exception ignored) {}
        }
    }

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public JobDtos.ResumeResponse upload(Authentication auth, @RequestParam("file") MultipartFile file) {
        ensureTable();
        return resumes.upload(users.getByEmail(auth.getName()), file);
    }
    @GetMapping("/latest")
    public ResponseEntity<?> latest(Authentication auth) {
        try {
            return ResponseEntity.ok(resumes.latest(users.getByEmail(auth.getName())));
        } catch (ResponseStatusException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return ResponseEntity.noContent().build();
            }
            throw e;
        }
    }
    @GetMapping("/{id}") public JobDtos.ResumeResponse get(Authentication auth, @PathVariable String id) { return resumes.get(users.getByEmail(auth.getName()), id); }
    @PutMapping("/{id}/skills")
    public JobDtos.ResumeResponse confirmSkills(Authentication auth, @PathVariable String id,
                                                @Valid @RequestBody JobDtos.ResumeSkillConfirmation confirmation) {
        return resumes.confirmSkills(users.getByEmail(auth.getName()), id, confirmation);
    }
}