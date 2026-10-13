package com.careercompass.controller;

import com.careercompass.dto.AuthDtos;
import com.careercompass.dto.JobDtos;
import com.careercompass.repository.AppUserRepository;
import com.careercompass.repository.JobApplicationRepository;
import com.careercompass.repository.RecommendationRepository;
import com.careercompass.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AppUserRepository users;
    private final JobApplicationRepository applications;
    private final RecommendationRepository recommendations;
    private final UserService userService;
    public AdminController(AppUserRepository users, JobApplicationRepository applications, RecommendationRepository recommendations, UserService userService) {
        this.users = users; this.applications = applications; this.recommendations = recommendations; this.userService = userService;
    }
    @GetMapping("/users") public java.util.List<AuthDtos.UserProfile> users() { return users.findAll().stream().map(userService::profile).toList(); }
    @GetMapping("/applications") public java.util.List<JobDtos.ActivityResponse> applications() {
        return applications.findAll().stream().map(item -> new JobDtos.ActivityResponse(item.getId(), JobDtos.JobResponse.from(item.getJob()), item.getStatus(), item.getAppliedAt())).toList();
    }
    @GetMapping("/stats") public java.util.Map<String, Long> stats() {
        return java.util.Map.of("users", users.count(), "applications", applications.count(), "recommendations", recommendations.count());
    }
}