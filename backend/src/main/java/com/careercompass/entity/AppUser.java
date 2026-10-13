package com.careercompass.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"))
public class AppUser {
    @Id @Column(length = 36) private String id = UUID.randomUUID().toString();
    private String name;
    @Column(nullable = false, length = 320) private String email;
    private String password;
    private String education;
    private Integer experience;
    private String location;
    private String preferredRole;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_skills", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "skill", nullable = false, length = 100)
    private Set<String> skills = new LinkedHashSet<>();
    @Column(nullable = false, length = 20)
    private String role = "USER";
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected AppUser() {}
    public AppUser(String name, String email, String password) { this.name = name; this.email = email; this.password = password; }
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    public Integer getExperience() { return experience; }
    public void setExperience(Integer experience) { this.experience = experience; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getPreferredRole() { return preferredRole; }
    public void setPreferredRole(String preferredRole) { this.preferredRole = preferredRole; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Set<String> getSkills() { return skills; }
    public void setSkills(Set<String> skills) { this.skills = skills == null ? new LinkedHashSet<>() : new LinkedHashSet<>(skills); }
    public LocalDateTime getCreatedAt() { return createdAt; }
}