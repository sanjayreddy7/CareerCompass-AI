package com.careercompass.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class RecommendationServiceTest {
    private final RecommendationService service = new RecommendationService();

    @Test void preprocessingRemovesStopWordsAndNormalizesTokens() {
        assertEquals(List.of("java", "spring", "boot", "apis"), service.preprocessText("Java and Spring Boot APIs"));
    }

    @Test void cosineSimilarityIsOneForIdenticalVectors() {
        assertEquals(1.0, service.calculateCosineSimilarity(Map.of("java", 2.0, "spring", 1.0), Map.of("java", 2.0, "spring", 1.0)), 0.000001);
    }

    @Test void matchingAndMissingSkillsAreCalculatedAgainstRequirements() {
        var matching = service.findMatchingSkills(Set.of("Java", "MySQL"), List.of("Java", "Spring Boot", "MySQL"));
        var missing = service.findMissingSkills(Set.of("Java", "MySQL"), List.of("Java", "Spring Boot", "MySQL"));
        assertEquals(List.of("Java", "MySQL"), matching);
        assertEquals(List.of("Spring Boot"), missing);
    }

    @Test void calculatedScoreCombinesSimilarityAndSkillCoverage() {
        assertEquals(75, service.calculateMatchScore(0.8, List.of("Java", "MySQL"), List.of("Java", "Spring", "MySQL")));
    }

    @Test void knownSkillsAreExtractedWithoutSubstringFalsePositives() {
        assertEquals(List.of("Java", "Spring Boot"), service.extractSkills("Built a Java service with Spring Boot", List.of("Java", "Spring Boot", "JavaScript")));
    }

    @Test void resumeExtractionFindsCommonSkillsEvenWhenNoCurrentJobRequiresThem() {
        assertEquals(List.of("JavaScript", "Python", "MongoDB"),
                service.extractSkills("Skills: JavaScript, Python and MongoDB", List.of("Java")));
    }

    @Test void resumeExtractionDoesNotTreatSqlAsPartOfPostgresql() {
        assertEquals(List.of("PostgreSQL"), service.extractSkills("PostgreSQL database experience", List.of("SQL", "PostgreSQL")));
    }
}