package com.careercompass.repository;

import com.careercompass.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, String> {
    List<JobApplication> findByUserIdOrderByAppliedAtDesc(String userId);
    boolean existsByUserIdAndJobId(String userId, String jobId);
}