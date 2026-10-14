package com.careercompass.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Lob;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resumes")
public class Resume {
    @Id @Column(length = 36) private String id = UUID.randomUUID().toString();
    @Column(nullable = false, length = 36)
    private String userId;
    private String fileName;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String extractedText;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "resume_extracted_skills", joinColumns = @JoinColumn(name = "resume_id"))
    @Column(name = "skill", nullable = false, length = 100)
    private List<String> extractedSkills = new ArrayList<>();
    @Column(nullable = false)
    private boolean skillsReviewed;
    @Column(nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();
    protected Resume() {}
    public Resume(AppUser user, String fileName, String extractedText, List<String> extractedSkills) {
        this.userId = user.getId(); this.fileName = fileName; this.extractedText = extractedText;
        this.extractedSkills = new ArrayList<>(extractedSkills);
    }
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getFileName() { return fileName; }
    public String getExtractedText() { return extractedText; }
    public List<String> getExtractedSkills() { return extractedSkills; }
    public boolean isSkillsReviewed() { return skillsReviewed; }
    public void setSkillsReviewed(boolean skillsReviewed) { this.skillsReviewed = skillsReviewed; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
}