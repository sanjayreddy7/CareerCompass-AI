package com.careercompass.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RecommendationDto(String id, JobDtos.JobResponse job, int matchScore, List<String> matchingSkills,
                                List<String> missingSkills, String explanation, LocalDateTime createdAt) {}