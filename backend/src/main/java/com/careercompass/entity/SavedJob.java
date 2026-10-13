package com.careercompass.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "saved_jobs", uniqueConstraints = @UniqueConstraint(name = "uk_saved_jobs_user_job", columnNames = {"user_id", "job_id"}))
public class SavedJob {
    @Id @Column(length = 36) private String id = UUID.randomUUID().toString();
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;
    @Column(name = "job_id", nullable = false, length = 36)
    private String jobId;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "job_snapshot", columnDefinition = "json")
    private Job job;
    @Column(nullable = false)
    private LocalDateTime savedAt = LocalDateTime.now();
    protected SavedJob() {}
    public SavedJob(AppUser user, Job job) { this.userId = user.getId(); this.jobId = job.getId(); this.job = job; }
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getJobId() { return jobId; }
    public Job getJob() { return job; }
    public LocalDateTime getSavedAt() { return savedAt; }
}