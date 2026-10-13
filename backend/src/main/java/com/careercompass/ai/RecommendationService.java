package com.careercompass.ai;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "and", "are", "as", "at", "be", "by", "for", "from", "in", "is", "it", "of", "on",
            "or", "our", "the", "to", "with", "will", "you", "your", "this", "that", "we", "have", "has"
    );
    private static final Pattern TOKEN_SPLITTER = Pattern.compile("[^a-z0-9+#.]+");
        private static final List<String> COMMON_SKILLS = List.of(
            "Java", "JavaScript", "TypeScript", "Python", "C", "C++", "C#", "Go", "Golang", "Ruby", "PHP", "Kotlin", "Swift", "Rust",
            "Spring", "Spring Boot", "React", "React.js", "Angular", "Vue", "Node.js", "Express.js", ".NET", "ASP.NET",
            "HTML", "CSS", "Tailwind CSS", "Bootstrap", "REST APIs", "GraphQL", "Microservices", "Hibernate", "JPA",
            "SQL", "MySQL", "PostgreSQL", "MongoDB", "Redis", "Oracle", "SQLite", "NoSQL", "Apache Kafka",
            "AWS", "Azure", "Google Cloud", "Docker", "Kubernetes", "Jenkins", "CI/CD", "Git", "Linux", "Terraform",
            "Machine Learning", "Deep Learning", "NLP", "Data Analysis", "Data Science", "TensorFlow", "PyTorch", "Pandas", "NumPy", "scikit-learn",
            "Selenium", "JUnit", "Testing", "Agile", "Scrum", "Figma", "Tableau", "Power BI", "Excel", "Communication"
        );

    public List<String> preprocessText(String text) {
        if (text == null || text.isBlank()) return List.of();
        return Arrays.stream(TOKEN_SPLITTER.split(text.toLowerCase(Locale.ROOT)))
                .filter(token -> token.length() > 1 && !STOP_WORDS.contains(token))
                .toList();
    }

    public Map<String, Double> calculateTfIdf(String document, List<String> corpus) {
        List<String> tokens = preprocessText(document);
        if (tokens.isEmpty()) return Map.of();
        Map<String, Long> counts = tokens.stream().collect(Collectors.groupingBy(token -> token, Collectors.counting()));
        Map<String, Double> vector = new HashMap<>();
        for (var entry : counts.entrySet()) {
            long documentsWithTerm = corpus.stream().filter(value -> preprocessText(value).contains(entry.getKey())).count();
            double idf = Math.log(1.0 + (corpus.size() + 1.0) / (documentsWithTerm + 1.0));
            vector.put(entry.getKey(), (entry.getValue() / (double) tokens.size()) * idf);
        }
        return vector;
    }

    public double calculateCosineSimilarity(Map<String, Double> left, Map<String, Double> right) {
        if (left.isEmpty() || right.isEmpty()) return 0.0;
        double dot = left.entrySet().stream().mapToDouble(entry -> entry.getValue() * right.getOrDefault(entry.getKey(), 0.0)).sum();
        double leftNorm = Math.sqrt(left.values().stream().mapToDouble(value -> value * value).sum());
        double rightNorm = Math.sqrt(right.values().stream().mapToDouble(value -> value * value).sum());
        return leftNorm == 0 || rightNorm == 0 ? 0.0 : dot / (leftNorm * rightNorm);
    }

    public int calculateMatchScore(double similarity, Collection<String> matchingSkills, Collection<String> requiredSkills) {
        if (requiredSkills.isEmpty()) return (int) Math.round(similarity * 100);
        double skillCoverage = matchingSkills.size() / (double) requiredSkills.size();
        return (int) Math.round(Math.min(1.0, similarity * 0.65 + skillCoverage * 0.35) * 100);
    }

    public List<String> extractSkills(String text, Collection<String> knownSkills) {
        String normalized = normalizeForSkillMatching(text);
        if (normalized.isBlank()) return List.of();
        String normalizedText = " " + normalized + " ";
        Set<String> vocabulary = new LinkedHashSet<>(COMMON_SKILLS);
        if (knownSkills != null) vocabulary.addAll(knownSkills);
        List<String> matched = vocabulary.stream()
                .filter(skill -> !skill.isBlank())
                .filter(skill -> normalizedText.contains(" " + normalizeForSkillMatching(skill) + " "))
            .toList();
        return matched.stream().filter(skill -> matched.stream().noneMatch(other ->
            other.length() > skill.length()
                && (" " + normalizeForSkillMatching(other) + " ").contains(" " + normalizeForSkillMatching(skill) + " ")))
            .toList();
    }

    private String normalizeForSkillMatching(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9+#]+", " ").trim().replaceAll("\\s+", " ");
    }

    public List<String> findMatchingSkills(Collection<String> profileSkills, Collection<String> requiredSkills) {
        Set<String> profile = profileSkills.stream().map(value -> value.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
        return requiredSkills.stream().filter(skill -> profile.contains(skill.toLowerCase(Locale.ROOT))).distinct().toList();
    }

    public List<String> findMissingSkills(Collection<String> profileSkills, Collection<String> requiredSkills) {
        Set<String> profile = profileSkills.stream().map(value -> value.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
        return requiredSkills.stream().filter(skill -> !profile.contains(skill.toLowerCase(Locale.ROOT))).distinct().toList();
    }
}