package com.careercompass.service;

import com.careercompass.ai.RecommendationService;
import com.careercompass.dto.JobDtos;
import com.careercompass.entity.AppUser;
import com.careercompass.entity.Resume;
import com.careercompass.repository.AppUserRepository;
import com.careercompass.repository.JobRepository;
import com.careercompass.repository.ResumeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@Service
public class ResumeService {
    private static final Logger log = LoggerFactory.getLogger(ResumeService.class);
    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private final ResumeRepository resumes;
    private final JobRepository jobs;
    private final RecommendationService ai;
    private final AppUserRepository users;

    @Autowired(required = false)
    private JdbcTemplate jdbc;

    public ResumeService(ResumeRepository resumes, JobRepository jobs, RecommendationService ai, AppUserRepository users) {
        this.resumes = resumes;
        this.jobs = jobs;
        this.ai = ai;
        this.users = users;
    }

    public JobDtos.ResumeResponse upload(AppUser user, MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() > MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a PDF smaller than 5 MB");
        }
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().trim();
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);

        boolean isPdfExtension = filename.toLowerCase(Locale.ROOT).endsWith(".pdf");
        boolean isPdfMime = contentType.isEmpty()
                || contentType.contains("pdf")
                || contentType.equals("application/octet-stream");

        if (!isPdfExtension || !isPdfMime) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF resumes (.pdf) are accepted");
        }

        log.info("Processing resume upload for user {}: {} (size: {} bytes, mime: {})",
                user.getEmail(), filename, file.getSize(), contentType);

        String extractedText;
        try (var pdf = Loader.loadPDF(file.getBytes())) {
            extractedText = new PDFTextStripper().getText(pdf);
        } catch (Exception exception) {
            log.error("Failed to parse PDF document for user {}: {}", user.getEmail(), exception.getMessage(), exception);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The uploaded file is not a readable PDF: " + exception.getMessage());
        }

        // Sanitize null characters and ligatures that break MySQL/PostgreSQL text columns
        extractedText = extractedText == null ? "" : extractedText.replace("\u0000", "").replace("\0", "").strip();
        if (extractedText.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No readable text found in this PDF");
        }
        if (extractedText.length() > 65000) {
            extractedText = extractedText.substring(0, 65000);
        }

        List<String> knownSkills = List.of();
        try {
            knownSkills = jobs.findAll().stream()
                    .filter(job -> job.getRequiredSkills() != null)
                    .flatMap(job -> job.getRequiredSkills().stream())
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(skill -> !skill.isEmpty())
                    .distinct()
                    .toList();
        } catch (Exception ex) {
            log.warn("Could not query job repository for known skills vocabulary: {}", ex.getMessage());
        }

        List<String> extractedSkills = ai.extractSkills(extractedText, knownSkills);
        // Ensure extracted skills do not exceed column length (100) or contain nulls
        extractedSkills = extractedSkills.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(skill -> !skill.isEmpty() && skill.length() <= 100)
                .distinct()
                .toList();

        if (jdbc != null) {
            try {
                jdbc.execute("CREATE TABLE IF NOT EXISTS resume_extracted_skills (" +
                        "resume_id VARCHAR(36) NOT NULL, " +
                        "skill VARCHAR(100) NOT NULL" +
                        ")");
            } catch (Exception ddlEx) {
                log.warn("Notice checking resume_extracted_skills schema: {}", ddlEx.getMessage());
            }
        }

        try {
            Resume resume = resumes.save(new Resume(user, filename, extractedText, extractedSkills));
            log.info("Successfully saved resume {} with {} extracted skills for user {}",
                    resume.getId(), extractedSkills.size(), user.getEmail());
            return toResponse(resume);
        } catch (Exception ex) {
            log.error("Database error saving resume for user {}: {}", user.getEmail(), ex.getMessage(), ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save resume record: " + ex.getMessage());
        }
    }
    public JobDtos.ResumeResponse latest(AppUser user) {
        return resumes.findFirstByUserIdOrderByUploadedAtDesc(user.getId()).map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No resume uploaded"));
    }

    public JobDtos.ResumeResponse get(AppUser user, String id) {
        Resume resume = resumes.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume not found"));
        return toResponse(resume);
    }

    public JobDtos.ResumeResponse confirmSkills(AppUser user, String id, JobDtos.ResumeSkillConfirmation confirmation) {
        Resume resume = resumes.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume not found"));
        var selected = new LinkedHashSet<>(confirmation.skills());
        if (!resume.getExtractedSkills().containsAll(selected)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected skills must come from the extracted resume skills");
        }
        var merged = new LinkedHashSet<>(user.getSkills());
        merged.addAll(selected);
        user.setSkills(merged);
        users.save(user);
        resume.setSkillsReviewed(true);
        return toResponse(resumes.save(resume));
    }

    private JobDtos.ResumeResponse toResponse(Resume resume) {
        return new JobDtos.ResumeResponse(resume.getId(), resume.getFileName(), resume.getExtractedText(),
                resume.getExtractedSkills(), resume.isSkillsReviewed(), resume.getUploadedAt());
    }
}