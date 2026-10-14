package com.careercompass.config;

import com.careercompass.entity.AppUser;
import com.careercompass.entity.Job;
import com.careercompass.repository.AppUserRepository;
import com.careercompass.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Set;

@Configuration
public class DataInitializer {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner ensureSchema(JdbcTemplate jdbc) {
        return args -> {
            try {
                jdbc.execute("CREATE TABLE IF NOT EXISTS resumes (" +
                        "id VARCHAR(36) NOT NULL PRIMARY KEY, " +
                        "user_id VARCHAR(36) NOT NULL, " +
                        "file_name VARCHAR(255), " +
                        "extracted_text TEXT, " +
                        "skills_reviewed BOOLEAN NOT NULL DEFAULT FALSE, " +
                        "uploaded_at DATETIME NOT NULL" +
                        ")");
                jdbc.execute("CREATE TABLE IF NOT EXISTS resume_extracted_skills (" +
                        "resume_id VARCHAR(36) NOT NULL, " +
                        "skill VARCHAR(100) NOT NULL" +
                        ")");
                jdbc.execute("CREATE TABLE IF NOT EXISTS recommendation_matching_skills (" +
                        "recommendation_id VARCHAR(36) NOT NULL, " +
                        "skill VARCHAR(100) NOT NULL" +
                        ")");
                jdbc.execute("CREATE TABLE IF NOT EXISTS recommendation_missing_skills (" +
                        "recommendation_id VARCHAR(36) NOT NULL, " +
                        "skill VARCHAR(100) NOT NULL" +
                        ")");
                log.info("Schema integrity verified: verified collection tables exist.");
            } catch (Exception ex) {
                log.warn("Schema initialization notice: {}", ex.getMessage());
            }
        };
    }

    @Bean
    @ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true")
    CommandLineRunner seedData(JobRepository jobs) {
        return args -> {
            if (jobs.count() == 0) {
                jobs.save(new Job("Java Backend Engineer", "Northstar Labs", "Build reliable APIs and distributed services for our product platform. Work with Java, Spring Boot, REST APIs, MySQL, Docker and cloud deployments.", "Bengaluru, India", 1200000, 2, Set.of("Java", "Spring Boot", "REST APIs", "MySQL", "Docker")));
                jobs.save(new Job("Full Stack Developer", "Olive Systems", "Ship customer-facing applications across the stack. Build React interfaces and Java Spring Boot services backed by PostgreSQL and automated tests.", "Remote, India", 1450000, 2, Set.of("Java", "Spring Boot", "React", "PostgreSQL", "Testing")));
                jobs.save(new Job("Machine Learning Associate", "Lattice AI", "Prepare datasets, evaluate models and deploy Python machine learning workflows. Familiarity with NLP, scikit-learn and cloud tooling is valued.", "Hyderabad, India", 1100000, 1, Set.of("Python", "Machine Learning", "NLP", "SQL", "AWS")));
                jobs.save(new Job("Frontend Engineer", "Brightworks", "Create accessible web experiences with React and TypeScript. Partner with design and API teams to improve performance and quality.", "Pune, India", 1000000, 1, Set.of("React", "JavaScript", "TypeScript", "CSS", "Accessibility")));
                jobs.save(new Job("Cloud Platform Developer", "Aster Digital", "Develop cloud-native Java services and deployment pipelines using Spring Boot, AWS, Docker, Kubernetes and observability tools.", "Chennai, India", 1800000, 3, Set.of("Java", "Spring Boot", "AWS", "Docker", "Kubernetes")));
                jobs.save(new Job("Data Analyst", "Fieldnote", "Translate business questions into reliable metrics. Write SQL, create dashboards and communicate findings with product teams.", "Remote, India", 850000, 0, Set.of("SQL", "Python", "Excel", "Data Analysis", "Tableau")));
                jobs.save(new Job("QA Automation Engineer", "Copperline Tech", "Build maintainable test automation for web services using Java, Selenium, REST APIs and CI pipelines.", "Bengaluru, India", 950000, 1, Set.of("Java", "Selenium", "Testing", "REST APIs", "CI/CD")));
                jobs.save(new Job("Product Software Engineer", "Common Thread", "Work across backend services and web product features with Java, Spring Boot, React, PostgreSQL and cloud infrastructure.", "Mumbai, India", 1600000, 2, Set.of("Java", "Spring Boot", "React", "PostgreSQL", "AWS")));
            }
        };
    }

    @Bean
    @ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true")
    CommandLineRunner seedDemoAccounts(AppUserRepository users, PasswordEncoder encoder) {
        return args -> {
            if (!users.existsByEmailIgnoreCase("demo@careercompass.dev")) {
                AppUser demo = new AppUser("Alex Morgan", "demo@careercompass.dev", encoder.encode("DemoPass123!"));
                demo.setLocation("Bengaluru"); demo.setPreferredRole("Java Developer"); demo.setExperience(2);
                demo.setEducation("B.Tech, Computer Science"); demo.setSkills(Set.of("Java", "Spring Boot", "MySQL", "React", "REST APIs"));
                users.save(demo);
            }
            if (!users.existsByEmailIgnoreCase("admin@careercompass.dev")) {
                AppUser admin = new AppUser("Career Compass Admin", "admin@careercompass.dev", encoder.encode("AdminPass123!"));
                admin.setRole("ADMIN"); users.save(admin);
            }
        };
    }
}