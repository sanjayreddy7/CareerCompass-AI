package com.careercompass.service;

import com.careercompass.dto.JobDtos;
import com.careercompass.entity.Job;
import com.careercompass.repository.JobRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JobService {
    private final JobRepository jobs;
    public JobService(JobRepository jobs) { this.jobs = jobs; }
    public List<Job> search(String query, String location, Integer minSalary, Integer experience, String skills) {
        Set<String> required = skills == null || skills.isBlank() ? Set.of() : java.util.Arrays.stream(skills.split(",")).map(value -> value.trim().toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
        return jobs.findAll().stream().filter(job -> query == null || query.isBlank() || (job.getTitle() + " " + job.getCompany() + " " + job.getDescription()).toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))
                .filter(job -> location == null || location.isBlank() || job.getLocation().toLowerCase(Locale.ROOT).contains(location.toLowerCase(Locale.ROOT)))
                .filter(job -> minSalary == null || (job.getSalary() != null && job.getSalary() >= minSalary))
                .filter(job -> experience == null || job.getExperienceRequired() == null || job.getExperienceRequired() <= experience)
                .filter(job -> required.isEmpty() || job.getRequiredSkills().stream().map(value -> value.toLowerCase(Locale.ROOT)).collect(Collectors.toSet()).containsAll(required))
                .toList();
    }
    public Job get(String id) { return jobs.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found")); }
    public Job create(JobDtos.JobRequest request) { return jobs.save(new Job(request.title(), request.company(), request.description(), request.location(), request.salary(), request.experienceRequired(), request.requiredSkills() == null ? Set.of() : request.requiredSkills())); }
    public Job update(String id, JobDtos.JobRequest request) {
        Job job = get(id); job.setTitle(request.title()); job.setCompany(request.company()); job.setDescription(request.description());
        job.setLocation(request.location()); job.setSalary(request.salary()); job.setExperienceRequired(request.experienceRequired()); job.setRequiredSkills(request.requiredSkills());
        return jobs.save(job);
    }
    public void delete(String id) { jobs.delete(get(id)); }
}