import axios from 'axios';

/**
 * Determine the backend API base URL.
 * All Spring Boot backend controllers are mapped under the '/api' prefix:
 * - /api/auth (login, register)
 * - /api/jobs (list, details, save, apply)
 * - /api/recommendations (job matching)
 * - /api/resumes (upload, analysis)
 * - /api/users (profile)
 * - /api/applications (tracker)
 * - /api/admin (management)
 *
 * Frontend components invoke routes such as '/auth/login', '/jobs', '/users/profile'.
 * Therefore, the effective baseURL must end with '/api'.
 */
function resolveBaseUrl() {
  const envUrl = (import.meta.env.VITE_API_URL || '').trim();
  // Strip any accidental trailing slashes
  const cleanUrl = envUrl.replace(/\/+$/, '');

  if (cleanUrl) {
    return cleanUrl.endsWith('/api') ? cleanUrl : `${cleanUrl}/api`;
  }

  // In development, default to '/api' which Vite dev/preview server proxies to http://localhost:8080
  if (import.meta.env.DEV) {
    return '/api';
  }

  // In production (e.g. deployed to Vercel/Netlify without VITE_API_URL configured),
  // fallback to the deployed Render backend so requests don't hit the static host with 404
  return 'https://careercompass-ai-p8sc.onrender.com/api';
}

export const API_BASE_URL = resolveBaseUrl();

const api = axios.create({
  baseURL: API_BASE_URL,
  // 60-second timeout to accommodate Render free-tier cold starts
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor: attach Authorization header if token exists,
// and ensure proper multipart headers for FormData uploads.
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // When sending FormData (e.g., resume PDF upload), delete Content-Type
    // so the browser sets multipart/form-data with the correct boundary.
    if (typeof FormData !== 'undefined' && config.data instanceof FormData) {
      delete config.headers['Content-Type'];
    }

    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor: handle token expiration and diagnostic logging
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const isAuthPage =
        typeof window !== 'undefined' &&
        (window.location.pathname === '/login' ||
          window.location.pathname === '/register');

      // Only force redirect if an authenticated session expired on a protected dashboard route
      if (!isAuthPage && typeof window !== 'undefined' && window.location.pathname.startsWith('/dashboard')) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.dispatchEvent(new Event('auth-logout'));
        window.location.assign('/login');
      }
    }

    // Diagnostic logging in development mode without leaking sensitive data
    if (import.meta.env.DEV) {
      const status = error.response?.status;
      const url = error.config?.url;
      const method = error.config?.method?.toUpperCase();
      console.warn(`[API] ${method} ${url} failed (${status || 'NETWORK_ERROR'}):`, error.message);
    }

    return Promise.reject(error);
  }
);

/**
 * Reusable user-friendly error message extractor.
 * Distinguishes between CORS/network failures, timeouts, 400 validation,
 * 401 unauthorized, 403 forbidden, 404 missing, 409 conflict, and 500 server errors.
 */
export function errorMessage(error) {
  if (!error) return 'An unexpected error occurred.';

  // Timeout error (e.g. Render spinning up from sleep)
  if (error.code === 'ECONNABORTED' || error.message?.toLowerCase().includes('timeout')) {
    return 'The server took too long to respond. The free Render instance may be waking up from sleep—please wait a few seconds and try again.';
  }

  // Network / CORS / offline error (no HTTP response received)
  if (!error.response) {
    if (typeof navigator !== 'undefined' && !navigator.onLine) {
      return 'You appear to be offline. Please check your internet connection.';
    }
    return 'Unable to reach the server. The backend may be starting up, unreachable, or blocked by CORS policy. Please try again shortly.';
  }

  const { status, data } = error.response;

  // Server returned structured error message
  if (data?.message && typeof data.message === 'string') {
    return data.message;
  }

  // Fallback by HTTP status code
  switch (status) {
    case 400:
      return 'Invalid request. Please check the entered information.';
    case 401:
      return 'Invalid email or password. Please check your credentials and try again.';
    case 403:
      return 'Access denied. You do not have permission to perform this action.';
    case 404:
      return 'The requested resource or endpoint was not found (404). Check backend route mappings.';
    case 409:
      return 'A conflict occurred. An account with this email may already exist.';
    case 500:
    case 502:
    case 503:
    case 504:
      return 'The backend service encountered an internal error. Please try again later.';
    default:
      return error.message || 'Something went wrong. Please try again.';
  }
}

export default api;
