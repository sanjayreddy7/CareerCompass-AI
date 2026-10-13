package com.careercompass.ai;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class SemanticSimilarityComponent {
    private static final Logger logger = LoggerFactory.getLogger(SemanticSimilarityComponent.class);
    private static final Pattern TOKEN_SPLITTER = Pattern.compile("[^a-z0-9+#.]+");
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "and", "are", "as", "at", "be", "by", "for", "from", "in", "is", "it",
            "of", "on", "or", "our", "the", "to", "with", "will", "you", "your", "this", "that",
            "we", "have", "has", "built", "build", "developed", "develop", "create", "created"
    );
    private static final Map<String, List<String>> CONCEPTS = Map.ofEntries(
            Map.entry("web_api", List.of("rest api", "rest apis", "web api", "web apis", "web service", "web services",
                    "http api", "http apis", "backend endpoint", "backend endpoints", "api endpoint", "api endpoints")),
            Map.entry("relational_database", List.of("sql", "mysql", "postgresql", "postgres", "oracle database", "relational database")),
            Map.entry("document_database", List.of("mongodb", "mongo database", "document database", "nosql")),
            Map.entry("cloud_computing", List.of("aws", "amazon web services", "azure", "google cloud", "cloud platform")),
            Map.entry("containerization", List.of("docker", "container", "containers", "containerization")),
            Map.entry("orchestration", List.of("kubernetes", "k8s", "container orchestration")),
            Map.entry("machine_learning", List.of("machine learning", "ml", "predictive modeling", "predictive modelling")),
            Map.entry("natural_language_processing", List.of("nlp", "natural language processing", "text processing")),
            Map.entry("data_visualization", List.of("tableau", "power bi", "data visualization", "data visualisation", "dashboards")),
            Map.entry("automated_testing", List.of("test automation", "automated testing", "selenium", "automation testing")),
            Map.entry("frontend_development", List.of("frontend", "front end", "user interface", "ui development", "web interface")),
            Map.entry("version_control", List.of("git", "github", "version control", "source control")),
            Map.entry("continuous_delivery", List.of("ci cd", "continuous integration", "continuous delivery", "continuous deployment")),
            Map.entry("message_streaming", List.of("kafka", "apache kafka", "event streaming", "message queues"))
    );

    private final RestClient restClient;
    private final String apiKey;
    private final String endpoint;

    public SemanticSimilarityComponent() {
        this(RestClient.builder().build(), "", "");
    }

    @Autowired
    public SemanticSimilarityComponent(
            RestClient.Builder restClientBuilder,
            @Value("${app.semantic.api-key:}") String apiKey,
            @Value("${app.semantic.endpoint:https://router.huggingface.co/hf-inference/models}") String endpoint,
            @Value("${app.semantic.model:BAAI/bge-small-en-v1.5}") String model) {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(45));
        this.restClient = restClientBuilder.requestFactory(requestFactory).build();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.endpoint = endpoint.replaceAll("/+$", "") + "/" + model;
    }

    SemanticSimilarityComponent(RestClient restClient, String apiKey, String endpoint) {
        this.restClient = restClient;
        this.apiKey = apiKey;
        this.endpoint = endpoint;
    }

    public List<Double> similarityScores(String profileText, List<String> jobTexts) {
        if (jobTexts.isEmpty()) return List.of();
        if (apiKey.isBlank()) return localSimilarityScores(profileText, jobTexts);
        try {
            return apiSimilarityScores(profileText, jobTexts);
        } catch (RestClientException | IllegalStateException exception) {
            logger.warn("Hugging Face embeddings failed; using the local semantic matcher instead: {}",
                    exception.getMessage());
            return localSimilarityScores(profileText, jobTexts);
        }
    }

    private List<Double> apiSimilarityScores(String profileText, List<String> jobTexts) {
        List<String> inputs = new ArrayList<>(jobTexts.size() + 1);
        inputs.add(profileText == null ? "" : profileText);
        inputs.addAll(jobTexts);
        JsonNode response = restClient.post()
                .uri(endpoint)
                .header("Authorization", "Bearer " + apiKey)
                .body(Map.of("inputs", inputs, "truncate", true, "normalize", true))
                .retrieve()
                .body(JsonNode.class);
        if (response == null || !response.isArray() || response.size() != inputs.size()) {
            throw new IllegalStateException("Hugging Face returned an unexpected embedding batch.");
        }

        List<double[]> embeddings = new ArrayList<>(response.size());
        for (JsonNode item : response) embeddings.add(readEmbedding(item));
        double[] profileEmbedding = embeddings.get(0);
        List<Double> scores = new ArrayList<>(jobTexts.size());
        for (int index = 1; index < embeddings.size(); index++) {
            scores.add(cosineSimilarity(profileEmbedding, embeddings.get(index)));
        }
        return scores;
    }

    private static double[] readEmbedding(JsonNode item) {
        if (item == null || !item.isArray() || item.isEmpty()) {
            throw new IllegalStateException("Hugging Face returned an empty embedding.");
        }
        if (item.get(0).isNumber()) return numericVector(item);

        double[] mean = null;
        int vectorCount = 0;
        for (JsonNode tokenVector : item) {
            double[] vector = numericVector(tokenVector);
            if (mean == null) mean = new double[vector.length];
            if (vector.length != mean.length) {
                throw new IllegalStateException("Hugging Face returned inconsistent embedding dimensions.");
            }
            for (int dimension = 0; dimension < mean.length; dimension++) mean[dimension] += vector[dimension];
            vectorCount++;
        }
        for (int dimension = 0; dimension < mean.length; dimension++) mean[dimension] /= vectorCount;
        return mean;
    }

    private static double[] numericVector(JsonNode values) {
        if (values == null || !values.isArray() || values.isEmpty()) {
            throw new IllegalStateException("Hugging Face returned an invalid embedding vector.");
        }
        double[] vector = new double[values.size()];
        for (int index = 0; index < values.size(); index++) {
            JsonNode value = values.get(index);
            if (!value.isNumber() || !Double.isFinite(value.doubleValue())) {
                throw new IllegalStateException("Hugging Face returned a non-numeric embedding value.");
            }
            vector[index] = value.doubleValue();
        }
        return vector;
    }

    private static double cosineSimilarity(double[] left, double[] right) {
        if (left.length != right.length) {
            throw new IllegalStateException("Hugging Face returned embeddings with different dimensions.");
        }
        double dot = 0.0;
        double leftNorm = 0.0;
        double rightNorm = 0.0;
        for (int index = 0; index < left.length; index++) {
            dot += left[index] * right[index];
            leftNorm += left[index] * left[index];
            rightNorm += right[index] * right[index];
        }
        if (leftNorm == 0.0 || rightNorm == 0.0) return 0.0;
        double cosine = dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
        return Math.max(0.0, Math.min(1.0, (cosine + 1.0) / 2.0));
    }

    private List<Double> localSimilarityScores(String profileText, List<String> jobTexts) {
        Map<String, Double> profileVector = vectorize(profileText);
        List<Double> scores = new ArrayList<>(jobTexts.size());
        for (String jobText : jobTexts) {
            scores.add(cosineSimilarity(profileVector, vectorize(jobText)));
        }
        return scores;
    }

    private Map<String, Double> vectorize(String text) {
        Map<String, Double> vector = new HashMap<>();
        String normalized = text == null ? "" : text.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9+#.]+", " ").trim().replaceAll("\\s+", " ");
        if (normalized.isEmpty()) return vector;

        List<String> tokens = List.of(TOKEN_SPLITTER.split(normalized));
        for (String token : tokens) {
            if (token.length() > 1 && !STOP_WORDS.contains(token)) {
                vector.merge("token:" + token, 1.0, Double::sum);
            }
        }
        for (var concept : CONCEPTS.entrySet()) {
            for (String phrase : concept.getValue()) {
                String normalizedPhrase = phrase.replaceAll("[^a-z0-9+#.]+", " ").trim().replaceAll("\\s+", " ");
                if ((" " + normalized + " ").contains(" " + normalizedPhrase + " ")) {
                    vector.put("concept:" + concept.getKey(), 2.0);
                    break;
                }
            }
        }
        return vector;
    }

    private static double cosineSimilarity(Map<String, Double> left, Map<String, Double> right) {
        if (left.isEmpty() || right.isEmpty()) return 0.0;
        double dot = left.entrySet().stream()
                .mapToDouble(entry -> entry.getValue() * right.getOrDefault(entry.getKey(), 0.0)).sum();
        double leftNorm = Math.sqrt(left.values().stream().mapToDouble(value -> value * value).sum());
        double rightNorm = Math.sqrt(right.values().stream().mapToDouble(value -> value * value).sum());
        if (leftNorm == 0.0 || rightNorm == 0.0) return 0.0;
        return Math.max(0.0, Math.min(1.0, dot / (leftNorm * rightNorm)));
    }
}
