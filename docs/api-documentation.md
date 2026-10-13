# API documentation

Base URL: `http://localhost:8080/api`. Protected routes require `Authorization: Bearer <JWT>`. JSON errors expose a `message` field.

| Method | Endpoint | Access | Behavior |
|---|---|---|---|
| `POST` | `/auth/register` | Public | Register and return token/profile |
| `POST` | `/auth/login` | Public | Authenticate and return token/profile |
| `GET`, `PUT` | `/users/profile` | User | Read or update current profile |
| `GET` | `/jobs` | Public | Search by optional `q`, `location`, `minSalary`, `experience`, `skills` |
| `GET` | `/jobs/{id}` | Public | Read job detail |
| `POST`, `PUT`, `DELETE` | `/jobs`, `/jobs/{id}` | Admin | Manage job listings |
| `POST`, `DELETE` | `/jobs/{jobId}/save` | User | Save or unsave a job |
| `GET` | `/jobs/saved` | User | List saved jobs |
| `POST` | `/jobs/{jobId}/apply` | User | Apply to a job |
| `GET` | `/applications` | User | List application history |
| `POST` | `/resumes/upload` | User | Upload a PDF as multipart field `file` |
| `GET` | `/resumes/latest`, `/resumes/{id}` | User | Read resume analysis |
| `PUT` | `/resumes/{id}/skills` | User | Confirm extracted skills |
| `GET` | `/recommendations` | User | Read recommendation history / generate on first request |
| `GET` | `/recommendations?refresh=true` | User | Recalculate recommendations |
| `GET` | `/recommendations/{jobId}` | User | Read a job's latest recommendation |
| `GET` | `/admin/users`, `/admin/applications`, `/admin/stats` | Admin | Admin reporting |

Authentication and endpoint names preserve the existing API. JSON request/response fields are defined by the DTOs under `backend/src/main/java/com/careercompass/dto`.
