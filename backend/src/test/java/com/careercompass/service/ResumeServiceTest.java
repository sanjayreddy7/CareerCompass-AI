package com.careercompass.service;

import com.careercompass.ai.RecommendationService;
import com.careercompass.entity.AppUser;
import com.careercompass.entity.Resume;
import com.careercompass.repository.AppUserRepository;
import com.careercompass.repository.JobRepository;
import com.careercompass.repository.ResumeRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import java.io.ByteArrayOutputStream;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.springframework.web.server.ResponseStatusException;

class ResumeServiceTest {
    @Test void uploadExtractsSkillsForReviewWithoutChangingTheProfile() throws Exception {
        ResumeRepository resumes = mock(ResumeRepository.class);
        JobRepository jobs = mock(JobRepository.class);
        AppUserRepository users = mock(AppUserRepository.class);
        RecommendationService ai = new RecommendationService();
        ResumeService service = new ResumeService(resumes, jobs, ai, users);
        AppUser user = new AppUser("Test User", "test@example.com", "hashed-password");
        user.setSkills(Set.of("Java"));
        when(jobs.findAll()).thenReturn(java.util.List.of());
        when(users.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(resumes.save(any(Resume.class))).thenAnswer(invocation -> invocation.getArgument(0));

        byte[] pdfBytes;
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                content.newLineAtOffset(50, 700);
                content.showText("Skills: JavaScript, Python and MongoDB");
                content.endText();
            }
            document.save(output);
            pdfBytes = output.toByteArray();
        }

        var response = service.upload(user, new MockMultipartFile("file", "resume.pdf", "application/pdf", pdfBytes));

        assertEquals(Set.of("Java"), user.getSkills());
        assertEquals(Set.of("JavaScript", "Python", "MongoDB"), Set.copyOf(response.extractedSkills()));
        assertEquals(false, response.skillsReviewed());
        verify(users, org.mockito.Mockito.never()).save(any(AppUser.class));
        verify(resumes).save(any());
    }

    @Test void onlySelectedExtractedSkillsAreAddedWhenUserConfirms() {
        ResumeRepository resumes = mock(ResumeRepository.class);
        JobRepository jobs = mock(JobRepository.class);
        AppUserRepository users = mock(AppUserRepository.class);
        Resume resume = mock(Resume.class);
        AppUser user = new AppUser("Test User", "test@example.com", "hashed-password");
        user.setSkills(Set.of("Java"));
        when(resumes.findByIdAndUserId("resume-id", user.getId())).thenReturn(java.util.Optional.of(resume));
        when(resume.getExtractedSkills()).thenReturn(java.util.List.of("Python", "MongoDB"));
        when(resumes.save(resume)).thenReturn(resume);
        when(resume.getId()).thenReturn("resume-id");
        when(resume.getFileName()).thenReturn("resume.pdf");
        when(resume.getUploadedAt()).thenReturn(java.time.LocalDateTime.now());
        when(resume.isSkillsReviewed()).thenReturn(true);
        when(users.save(user)).thenReturn(user);
        ResumeService service = new ResumeService(resumes, jobs, new RecommendationService(), users);

        var result = service.confirmSkills(user, "resume-id", new com.careercompass.dto.JobDtos.ResumeSkillConfirmation(Set.of("Python")));

        assertEquals(Set.of("Java", "Python"), user.getSkills());
        assertEquals(true, result.skillsReviewed());
        verify(users).save(user);
        verify(resumes).save(resume);
        verify(resume).setSkillsReviewed(true);
        assertThrows(ResponseStatusException.class, () -> service.confirmSkills(user, "resume-id",
                new com.careercompass.dto.JobDtos.ResumeSkillConfirmation(Set.of("Docker"))));
        verify(users, org.mockito.Mockito.times(1)).save(user);
    }
}