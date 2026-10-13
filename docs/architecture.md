# Architecture

## Runtime components

- `frontend/` is the existing React 19/Vite application. Route-level screens are under `src/pages`, reusable UI under `src/components`, page styles under `src/styles`, and the shared Axios client under `src/services/api.js`.
- `backend/` is the existing Java 17/Spring Boot application. Its single entry point is `com.careercompass.CareerCompassApplication`.
- Spring MVC controllers retain the `/api/...` route names. Services contain application and recommendation logic. Repositories use Spring Data JPA with MySQL.
- `backend/src/main/resources/application.yml` centralizes environment-based MySQL, upload, JWT, CORS and Hibernate settings.

## Persistence

JPA entity mappings define users, jobs, resumes, applications, saved jobs and recommendations. User/job/resume/recommendation skill sets are stored in collection tables. Application, saved-job and recommendation records keep JSON job snapshots so their displayed listing remains stable if a live job later changes or is removed. MySQL schema creation/update is handled by Hibernate with `spring.jpa.hibernate.ddl-auto=update`.

The old implementation used MongoDB. This restructuring does not delete MongoDB files or external database data, and it does not implicitly migrate stored MongoDB records into MySQL.

## Recommendation flow

1. Combine profile skills, preferred role, education and latest resume text.
2. Normalize/tokenize text, remove stop words and extract known job skills.
3. Calculate TF-IDF vectors and cosine similarity across the user profile and job descriptions.
4. Blend text similarity with required-skill coverage and apply the existing experience adjustment.
5. Persist ranked recommendation snapshots with matching/missing skills and an explanation.

## Security and integration

The frontend Axios client reads the JWT from local storage and attaches a Bearer token. The backend uses stateless Spring Security and the existing JWT filter. CORS origins are configurable through `APP_CORS_ALLOWED_ORIGINS`; local development defaults to Vite on localhost and 127.0.0.1, port 5173. PDF resume uploads are multipart requests to the existing `/api/resumes/upload` endpoint.
