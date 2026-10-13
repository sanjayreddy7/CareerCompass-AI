package com.careercompass.dto;

import com.careercompass.entity.Job;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.Set;

public final class JobDtos {
    private JobDtos() {}
    public record JobRequest(@NotBlank String title, @NotBlank String company, @NotBlank String description,
                             @NotBlank String location, @PositiveOrZero Integer salary,
                             @PositiveOrZero Integer experienceRequired, Set<String> requiredSkills) {}
    public record JobResponse(String id, String title, String company, String description, String location,
                              Integer salary, Integer experienceRequired, Set<String> requiredSkills, LocalDateTime createdAt) {
        public static JobResponse from(Job job) {
            return new JobResponse(job.getId(), job.getTitle(), job.getCompany(), job.getDescription(), job.getLocation(),
                    job.getSalary(), job.getExperienceRequired(), job.getRequiredSkills(), job.getCreatedAt());
        }
    }
    public record ActivityResponse(String id, JobResponse job, String status, LocalDateTime createdAt) {}
    public record ResumeSkillConfirmation(@NotEmpty Set<@NotBlank String> skills) {}
    public record ResumeResponse(String id, String fileName, String extractedText, java.util.List<String> extractedSkills,
                                 boolean skillsReviewed, LocalDateTime uploadedAt) {}
}