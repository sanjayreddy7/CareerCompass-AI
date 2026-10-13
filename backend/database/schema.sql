-- Create the local database before starting Spring Boot.
-- JPA manages and updates application tables from the entity mappings.
CREATE DATABASE IF NOT EXISTS career_compass
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;
