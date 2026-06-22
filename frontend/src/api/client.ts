import axios from 'axios';

// Falls back to localhost:8080 for local dev against docker-compose;
// production deploys override this via the VITE_API_BASE_URL build-time env var.
const baseURL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

export const apiClient = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
});
