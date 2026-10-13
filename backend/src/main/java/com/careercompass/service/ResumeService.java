package com.careercompass.service;

import com.careercompass.ai.RecommendationService;
import com.careercompass.dto.JobDtos;
import com.careercompass.entity.AppUser;
import com.careercompass.entity.Resume;
import com.careercompass.repository.AppUserRepository;
import com.careercompass.repository.JobRepository;
import com.careercompass.repository.ResumeRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class ResumeService {
    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private final ResumeRepository resumes;
    private final JobRepository jobs;
    private final RecommendationService ai;
    private final AppUserRepository users;
    public ResumeService(ResumeRepository resumes, JobRepository jobs, RecommendationService ai, AppUserRepository users) { this.resumes = resumes; this.jobs = jobs; this.ai = ai; this.users = users; }
    public JobDtos.ResumeResponse upload(AppUser user, MultipartFile file) {
        if (file.isEmpty() || file.getSize() > MAX_BYTES) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a PDF smaller than 5 MB");
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        if (!filename.toLowerCase().endsWith(".pdf") || (file.getContentType() != null && !file.getContentType().equalsIgnoreCase("application/pdf")))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF resumes are accepted");
        try (var pdf = Loader.loadPDF(file.getBytes())) {
            String extractedText = new PDFTextStripper().getText(pdf).strip();
            if (extractedText.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No readable text found in this PDF");
            List<String> knownSkills = jobs.findAll().stream().flatMap(job -> job.getRequiredSkills().stream()).distinct().toList();
            List<String> extractedSkills = ai.extractSkills(extractedText, knownSkills);
            Resume resume = resumes.save(new Resume(user, filename, extractedText, extractedSkills));
            return toResponse(resume);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The uploaded file is not a readable PDF");
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