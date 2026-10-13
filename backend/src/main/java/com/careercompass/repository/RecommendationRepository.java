package com.careercompass.repository;

import com.careercompass.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RecommendationRepository extends JpaRepository<Recommendation, String> {
    List<Recommendation> findByUserIdOrderByMatchScoreDescCreatedAtDesc(String userId);
    List<Recommendation> findByUserIdOrderByCreatedAtDesc(String userId);
    Optional<Recommendation> findFirstByUserIdAndJobIdOrderByCreatedAtDesc(String userId, String jobId);
}