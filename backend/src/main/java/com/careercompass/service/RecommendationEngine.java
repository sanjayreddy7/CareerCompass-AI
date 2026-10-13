package com.careercompass.service;

import com.careercompass.ai.RecommendationService;
import com.careercompass.dto.JobDtos;
import com.careercompass.dto.RecommendationDto;
import com.careercompass.entity.AppUser;
import com.careercompass.entity.Job;
import com.careercompass.entity.Recommendation;
import com.careercompass.entity.Resume;
import com.careercompass.repository.JobRepository;
import com.careercompass.repository.RecommendationRepository;
import com.careercompass.repository.ResumeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
public class RecommendationEngine {
    private final RecommendationService ai;
    private final JobRepository jobs;
    private final ResumeRepository resumes;
    private final RecommendationRepository recommendations;
    public RecommendationEngine(RecommendationService ai, JobRepository jobs, ResumeRepository resumes, RecommendationRepository recommendations) {
        this.ai = ai; this.jobs = jobs; this.resumes = resumes; this.recommendations = recommendations;
    }
    public List<RecommendationDto> recommend(AppUser user) {
        List<Job> allJobs = jobs.findAll();
        String resumeText = resumes.findFirstByUserIdOrderByUploadedAtDesc(user.getId()).map(Resume::getExtractedText).orElse("");
        String profileText = String.join(" ", user.getSkills()) + " " + Objects.toString(user.getPreferredRole(), "") + " " + Objects.toString(user.getEducation(), "") + " " + resumeText;
        List<String> corpus = new ArrayList<>();
        corpus.add(profileText);
        allJobs.forEach(job -> corpus.add(job.getTitle() + " " + job.getDescription() + " " + String.join(" ", job.getRequiredSkills())));
        Map<String, Double> profileVector = ai.calculateTfIdf(profileText, corpus);
        Set<String> profileSkills = new LinkedHashSet<>(user.getSkills());
        profileSkills.addAll(ai.extractSkills(resumeText, allJobs.stream().flatMap(job -> job.getRequiredSkills().stream()).distinct().toList()));
        List<RecommendationDto> ranked = new ArrayList<>();
        for (Job job : allJobs) {
            String jobText = job.getTitle() + " " + job.getDescription() + " " + String.join(" ", job.getRequiredSkills());
            double similarity = ai.calculateCosineSimilarity(profileVector, ai.calculateTfIdf(jobText, corpus));
            List<String> matching = ai.findMatchingSkills(profileSkills, job.getRequiredSkills().stream().toList());
            List<String> missing = ai.findMissingSkills(profileSkills, job.getRequiredSkills().stream().toList());
            int score = ai.calculateMatchScore(similarity, matching, job.getRequiredSkills());
            if (user.getExperience() != null && job.getExperienceRequired() != null && user.getExperience() >= job.getExperienceRequired()) score = Math.min(100, score + 5);
            String explanation = matching.isEmpty()
                    ? "This role aligns with your profile keywords. Add relevant skills such as " + String.join(", ", missing.stream().limit(3).toList()) + " to strengthen your match."
                    : "Your " + String.join(", ", matching) + " skills match this role. " + (missing.isEmpty() ? "Your profile covers all listed skills." : "Develop " + String.join(", ", missing) + " to improve your suitability.");
            Recommendation saved = recommendations.save(new Recommendation(user, job, score, matching, missing, explanation));
            ranked.add(toDto(saved));
        }
        return ranked.stream().sorted(Comparator.comparingInt(RecommendationDto::matchScore).reversed()).toList();
    }
        public List<RecommendationDto> history(AppUser user) {
        Map<String, Recommendation> newestByJob = new LinkedHashMap<>();
        recommendations.findByUserIdOrderByCreatedAtDesc(user.getId())
            .forEach(item -> newestByJob.putIfAbsent(item.getJobId(), item));
        return newestByJob.values().stream().map(this::toDto)
            .sorted(Comparator.comparingInt(RecommendationDto::matchScore).reversed()).toList();
        }
    public RecommendationDto get(AppUser user, String jobId) {
        return recommendations.findFirstByUserIdAndJobIdOrderByCreatedAtDesc(user.getId(), jobId).map(this::toDto)
                .orElseGet(() -> recommend(user).stream().filter(item -> item.job().id().equals(jobId)).findFirst()
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recommendation not found")));
    }
    private RecommendationDto toDto(Recommendation item) {
        return new RecommendationDto(item.getId(), JobDtos.JobResponse.from(item.getJob()), item.getMatchScore(), item.getMatchingSkills(), item.getMissingSkills(), item.getExplanation(), item.getCreatedAt());
    }
}