package com.careercompass.repository;

import com.careercompass.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, String> {
    Optional<Resume> findFirstByUserIdOrderByUploadedAtDesc(String userId);
    Optional<Resume> findByIdAndUserId(String id, String userId);
}