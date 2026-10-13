# AI Job Recommendation System

**AI-Based Job Recommendation System Using Java, Spring Boot, NLP and Machine Learning**

AI Job Recommendation System is a single-workspace full-stack project. The React/Vite application provides landing, registration, login, onboarding, profile, resume upload, job search, recommendations, skill-gap, saved-job, application-tracking and admin screens. The Spring Boot API parses PDF resumes, extracts relevant skills, ranks jobs using TF-IDF cosine similarity and skill coverage, and exposes JWT-protected REST endpoints.

## Project layout

```text
AI-Job-Recommendation-System/         # Open this workspace folder in VS Code
|-- frontend/
|   |-- public/
|   `-- src/
|       |-- assets/
|       |-- components/               # Reusable UI
|       |-- pages/                    # Route-level screens
|       |-- services/                 # Centralized API client
|       `-- styles/
|-- backend/
|   |-- database/                     # MySQL schema and optional sample data
|   |-- scripts/                      # Windows local launcher
|   `-- src/main/java/com/careercompass/
|       |-- ai/ config/ controller/ dto/ entity/
|       |-- exception/ repository/ security/ service/
|       `-- ...
|-- docs/
|-- .vscode/
|-- .gitignore
`-- README.md
```

The workspace is the single project root; source files were not copied into a second frontend or backend. The backend package remains `com.careercompass` to preserve the existing Java package structure and API.

## Requirements

- Java 17+
- MySQL 8+
- Node.js 20.19+ and npm
- Apache Maven 3.8+ (the Windows launcher downloads Maven 3.9.11 to `%LOCALAPPDATA%\CareerCompass\tools` if Maven is not on `PATH`)

## Configure MySQL and the backend

1. Start your local MySQL 8 server.
2. Create the database (the default JDBC URL can also create it when the configured user has permission):

   ```powershell
   mysql.exe -u root -p -e "CREATE DATABASE IF NOT EXISTS career_compass CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
   ```

3. Copy `backend\.env.example` to `backend\.env` and replace the MySQL username/password placeholders; set a private `APP_JWT_SECRET` of at least 32 bytes. `backend\.env` is ignored by Git. The local launcher loads it; environment variables already set in the terminal take precedence.
4. Start the API in a VS Code terminal:

   ```powershell
   powershell.exe -NoProfile -ExecutionPolicy Bypass -File backend\scripts\run-local.ps1
   ```

The API runs at `http://localhost:8080`. JPA creates/updates tables from the entity mappings (`ddl-auto=update`). The optional `backend\database\schema.sql` creates the database; `backend\database\seed-data.sql` inserts sample jobs after the first backend start has created the tables. Alternatively, the launcher enables the existing Java demo initializer on an empty local database, including demo accounts; it does not enable demo data by default for a remote MySQL URL.

Local demo login (development only): `demo@careercompass.dev` / `DemoPass123!`. The demo initializer is enabled by the local launcher; it is disabled by default when running Spring Boot directly.

## Configure and start the frontend

Open a second VS Code terminal:

```powershell
Copy-Item frontend\.env.example frontend\.env
npm.cmd install --prefix frontend
npm.cmd run dev --prefix frontend
```

Open the Vite URL shown in the terminal, normally `http://localhost:5173`. The Vite development server proxies `/api` to `http://localhost:8080`. To use another API origin, set `VITE_API_URL` in `frontend\.env`, including the `/api` suffix, for example `VITE_API_URL=http://localhost:8080/api`.

## Build and test

```powershell
npm.cmd run lint --prefix frontend
npm.cmd run build --prefix frontend
mvn.cmd -f backend\pom.xml test
```

If Maven is not on `PATH`, run the backend tests with the launcher-installed Maven:

```powershell
& "$env:LOCALAPPDATA\CareerCompass\tools\apache-maven-3.9.11\bin\mvn.cmd" -f backend\pom.xml test
```

## Configuration

| Variable | Purpose | Default |
|---|---|---|
| `DB_URL` | MySQL JDBC URL | Local `career_compass` database |
| `DB_USERNAME` | MySQL user | Required |
| `DB_PASSWORD` | MySQL password | Required (may be empty only if your local MySQL account has no password) |
| `APP_JWT_SECRET` | JWT signing key | Required outside the local launcher |
| `APP_DEMO_DATA` | Seed demo listings/accounts when database is empty | `false` |
| `APP_CORS_ALLOWED_ORIGINS` | Comma-separated allowed browser origins | Local Vite origins |
| `VITE_API_URL` | Frontend API base URL (must include `/api`) | Vite proxy `/api` |

See [docs/setup-guide.md](docs/setup-guide.md), [docs/architecture.md](docs/architecture.md), and [docs/api-documentation.md](docs/api-documentation.md) for additional details. MySQL/JPA is the active persistence implementation. The previous MongoDB data store is not deleted or automatically migrated; export/migrate any existing MongoDB data separately before retiring it.
