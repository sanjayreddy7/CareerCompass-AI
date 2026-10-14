package com.careercompass.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Convert;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "recommendations")
public class Recommendation {
    @Id @Column(length = 36) private String id = UUID.randomUUID().toString();
    @Column(nullable = false, length = 36)
    private String userId;
    @Column(nullable = false, length = 36)
    private String jobId;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "job_snapshot", columnDefinition = "json")
    private Job job;
    @Column(nullable = false)
    private Integer matchScore;
    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> matchingSkills;
    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> missingSkills;
    @Column(columnDefinition = "TEXT")
    private String explanation;
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    protected Recommendation() {}
    public Recommendation(AppUser user, Job job, Integer matchScore, List<String> matchingSkills, List<String> missingSkills, String explanation) {
        this.userId = user.getId(); this.jobId = job.getId(); this.job = job; this.matchScore = matchScore; this.matchingSkills = matchingSkills;
        this.missingSkills = missingSkills; this.explanation = explanation;
    }
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getJobId() { return jobId; }
    public Job getJob() { return job; }
    public Integer getMatchScore() { return matchScore; }
    public List<String> getMatchingSkills() { return matchingSkills; }
    public List<String> getMissingSkills() { return missingSkills; }
    public String getExplanation() { return explanation; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}