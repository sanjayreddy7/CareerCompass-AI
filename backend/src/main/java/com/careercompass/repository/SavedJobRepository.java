package com.careercompass.repository;

import com.careercompass.entity.SavedJob;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SavedJobRepository extends JpaRepository<SavedJob, String> {
    List<SavedJob> findByUserIdOrderBySavedAtDesc(String userId);
    boolean existsByUserIdAndJobId(String userId, String jobId);
    void deleteByUserIdAndJobId(String userId, String jobId);
}