# VS Code setup guide

## One-time setup

1. Open the project workspace root in VS Code.
2. Install Java 17+, MySQL 8+, Node.js 20+, and npm.
3. Create the MySQL database:

   ```powershell
   mysql.exe -u root -p -e "CREATE DATABASE IF NOT EXISTS career_compass CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
   ```

4. Copy `backend\.env.example` to `backend\.env`; replace the MySQL username/password placeholders and configure a private JWT secret. Never commit the real `.env`.
5. Copy `frontend\.env.example` to `frontend\.env`. The default Vite proxy needs no additional setting.

## Start the application

Use two VS Code integrated terminals. Start the backend first:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File backend\scripts\run-local.ps1
```

Then start the frontend:

```powershell
npm.cmd install --prefix frontend
npm.cmd run dev --prefix frontend
```

Open `http://localhost:5173`. The backend API is available at `http://localhost:8080`.

## Verification

```powershell
npm.cmd run lint --prefix frontend
npm.cmd run build --prefix frontend
mvn.cmd -f backend\pom.xml test
```

For direct Spring Boot runs, export `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `APP_JWT_SECRET` in the terminal because Spring Boot does not load `.env` files itself. The PowerShell launcher reads `backend\.env` and generates a temporary local JWT key only if none is configured.

## Database scripts and migration note

`backend\database\schema.sql` creates the MySQL database. The Hibernate mappings create/update tables when the backend starts. Run `backend\database\seed-data.sql` only after the application has created its tables and when you want SQL sample job data.

MongoDB data from an older installation is left intact but is not automatically imported into MySQL. Back up and migrate it explicitly if you need to keep previously stored users, jobs, resumes or activity records.
