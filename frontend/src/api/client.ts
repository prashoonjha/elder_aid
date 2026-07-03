import axios from 'axios';

// Falls back to localhost:8080 for local dev against docker-compose;
// production deploys override this via the VITE_API_BASE_URL build-time env var.
const baseURL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

export const apiClient = axios.create({
  baseURL,
  // Send cookies (the httpOnly refresh cookie) on cross-origin requests to
  // the API. Without this the browser wouldn't attach the cookie at all.
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Only the short-lived access token lives in memory now. The refresh token
// is held by the browser as an httpOnly cookie we can't (and don't want to)
// read from JavaScript - that's what lets a session survive a page reload
// while staying out of reach of any XSS payload.
let accessToken: string | null = null;

export function setAccessToken(token: string | null) {
  accessToken = token;
}

export function getAccessToken() {
  return accessToken;
}

// AuthContext registers a listener here so client.ts can trigger a logout
// without importing React/Context code into a plain module.
let sessionExpiredListener: (() => void) | null = null;

export function onSessionExpired(listener: () => void) {
  sessionExpiredListener = listener;
}

apiClient.interceptors.request.use((config) => {
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});

let refreshInFlight: Promise<string | null> | null = null;

// Calls /refresh relying purely on the httpOnly cookie - no body, no stored
// refresh token. Returns the new access token, or null if the cookie is
// missing/expired (i.e. no valid session to restore).
export async function refreshAccessToken(): Promise<string | null> {
  // Multiple requests can 401 around the same moment - without this guard
  // each would trigger its own refresh, and the backend only honors the
  // first (refresh tokens are single-use). Sharing one in-flight promise
  // means everyone waits for, and reuses, the same refresh.
  if (!refreshInFlight) {
    refreshInFlight = axios
      .post(`${baseURL}/api/auth/refresh`, null, { withCredentials: true })
      .then((response) => {
        setAccessToken(response.data.accessToken);
        return response.data.accessToken as string;
      })
      .catch(() => {
        setAccessToken(null);
        return null;
      })
      .finally(() => {
        refreshInFlight = null;
      });
  }
  return refreshInFlight;
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    if (error.response?.status === 401 && !originalRequest._retried) {
      originalRequest._retried = true;
      const newAccessToken = await refreshAccessToken();
      if (newAccessToken) {
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        return apiClient(originalRequest);
      }
      // Refresh failed - no valid session to restore, so tell the app.
      sessionExpiredListener?.();
    }
    return Promise.reject(error);
  },
);
