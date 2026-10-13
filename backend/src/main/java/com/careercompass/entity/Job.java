package com.careercompass.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "jobs")
public class Job {
    @Id @Column(length = 36) private String id = UUID.randomUUID().toString();
    private String title;
    private String company;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String location;
    private Integer salary;
    private Integer experienceRequired;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "job_required_skills", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "skill", nullable = false, length = 100)
    private Set<String> requiredSkills = new LinkedHashSet<>();
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Job() {}
    public Job(String title, String company, String description, String location, Integer salary, Integer experienceRequired, Set<String> requiredSkills) {
        this.title = title; this.company = company; this.description = description; this.location = location;
        this.salary = salary; this.experienceRequired = experienceRequired; this.requiredSkills = new LinkedHashSet<>(requiredSkills);
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Integer getSalary() { return salary; }
    public void setSalary(Integer salary) { this.salary = salary; }
    public Integer getExperienceRequired() { return experienceRequired; }
    public void setExperienceRequired(Integer experienceRequired) { this.experienceRequired = experienceRequired; }
    public Set<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(Set<String> requiredSkills) { this.requiredSkills = requiredSkills == null ? new LinkedHashSet<>() : new LinkedHashSet<>(requiredSkills); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}