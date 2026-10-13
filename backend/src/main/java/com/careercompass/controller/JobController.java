package com.careercompass.controller;

import com.careercompass.dto.JobDtos;
import com.careercompass.entity.AppUser;
import com.careercompass.entity.JobApplication;
import com.careercompass.entity.SavedJob;
import com.careercompass.repository.JobApplicationRepository;
import com.careercompass.repository.SavedJobRepository;
import com.careercompass.service.JobService;
import com.careercompass.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private final JobService jobs;
    private final UserService users;
    private final SavedJobRepository savedJobs;
    private final JobApplicationRepository applications;
    public JobController(JobService jobs, UserService users, SavedJobRepository savedJobs, JobApplicationRepository applications) {
        this.jobs = jobs; this.users = users; this.savedJobs = savedJobs; this.applications = applications;
    }
    @GetMapping public List<JobDtos.JobResponse> list(@RequestParam(required = false) String q, @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer minSalary, @RequestParam(required = false) Integer experience,
            @RequestParam(required = false) String skills) {
        return jobs.search(q, location, minSalary, experience, skills).stream().map(JobDtos.JobResponse::from).toList();
    }
    @GetMapping("/{id}") public JobDtos.JobResponse get(@PathVariable String id) { return JobDtos.JobResponse.from(jobs.get(id)); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public JobDtos.JobResponse create(@Valid @RequestBody JobDtos.JobRequest request) { return JobDtos.JobResponse.from(jobs.create(request)); }
    @PutMapping("/{id}") public JobDtos.JobResponse update(@PathVariable String id, @Valid @RequestBody JobDtos.JobRequest request) { return JobDtos.JobResponse.from(jobs.update(id, request)); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable String id) { jobs.delete(id); }
    @PostMapping("/{jobId}/save") @ResponseStatus(HttpStatus.CREATED)
    public void save(@PathVariable String jobId, Authentication auth) {
        AppUser user = users.getByEmail(auth.getName()); var job = jobs.get(jobId);
        if (savedJobs.existsByUserIdAndJobId(user.getId(), jobId)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Job already saved");
        savedJobs.save(new SavedJob(user, job));
    }
    @GetMapping("/saved") public List<JobDtos.ActivityResponse> saved(Authentication auth) {
        return savedJobs.findByUserIdOrderBySavedAtDesc(users.getByEmail(auth.getName()).getId()).stream()
                .map(item -> new JobDtos.ActivityResponse(item.getId(), JobDtos.JobResponse.from(item.getJob()), "SAVED", item.getSavedAt())).toList();
    }
    @DeleteMapping("/{jobId}/save") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsave(@PathVariable String jobId, Authentication auth) { savedJobs.deleteByUserIdAndJobId(users.getByEmail(auth.getName()).getId(), jobId); }
    @PostMapping("/{jobId}/apply") @ResponseStatus(HttpStatus.CREATED)
    public JobDtos.ActivityResponse apply(@PathVariable String jobId, Authentication auth) {
        AppUser user = users.getByEmail(auth.getName()); var job = jobs.get(jobId);
        if (applications.existsByUserIdAndJobId(user.getId(), jobId)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Application already exists");
        JobApplication application = applications.save(new JobApplication(user, job));
        return new JobDtos.ActivityResponse(application.getId(), JobDtos.JobResponse.from(job), application.getStatus(), application.getAppliedAt());
    }
}