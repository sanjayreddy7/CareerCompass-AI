package com.careercompass.controller;

import com.careercompass.dto.JobDtos;
import com.careercompass.repository.JobApplicationRepository;
import com.careercompass.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
    private final JobApplicationRepository applications;
    private final UserService users;
    public ApplicationController(JobApplicationRepository applications, UserService users) { this.applications = applications; this.users = users; }
    @GetMapping public java.util.List<JobDtos.ActivityResponse> list(Authentication auth) {
        return applications.findByUserIdOrderByAppliedAtDesc(users.getByEmail(auth.getName()).getId()).stream()
                .map(item -> new JobDtos.ActivityResponse(item.getId(), JobDtos.JobResponse.from(item.getJob()), item.getStatus(), item.getAppliedAt())).toList();
    }
}