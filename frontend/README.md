# AI Job Recommendation System frontend

This is the project's existing React/Vite application. It contains route-level pages under `src/pages`, reusable UI under `src/components`, centralized Axios API access in `src/services/api.js`, and styling in `src/styles`.

From the workspace root:

```powershell
npm.cmd install --prefix frontend
npm.cmd run dev --prefix frontend
npm.cmd run lint --prefix frontend
npm.cmd run build --prefix frontend
```

Vite proxies `/api` to `http://localhost:8080` during local development. Configure `VITE_API_URL` in `frontend\.env` only when using a different API origin.
