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

// Tokens live only in memory - never localStorage/sessionStorage - so a
// malicious script injected via XSS has nothing to read. The real cost:
// a full page reload clears them, requiring a fresh login. That tradeoff
// was a deliberate choice, not an oversight.
let accessToken: string | null = null;
let refreshToken: string | null = null;

export function setTokens(tokens: { accessToken: string; refreshToken: string } | null) {
  accessToken = tokens?.accessToken ?? null;
  refreshToken = tokens?.refreshToken ?? null;
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

async function refreshAccessToken(): Promise<string | null> {
  if (!refreshToken) return null;

  // Multiple requests can 401 around the same moment (e.g. a page that
  // fires several API calls at once) - without this guard, each would
  // trigger its own refresh call, and the backend would only honor the
  // first one (refresh tokens are single-use). Sharing one in-flight
  // promise means everyone waits for, and reuses, the same refresh.
  if (!refreshInFlight) {
    refreshInFlight = axios
      .post(`${baseURL}/api/auth/refresh`, { refreshToken })
      .then((response) => {
        const newTokens = { accessToken: response.data.accessToken, refreshToken: response.data.refreshToken };
        setTokens(newTokens);
        return newTokens.accessToken;
      })
      .catch(() => {
        setTokens(null);
        sessionExpiredListener?.();
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
    }
    return Promise.reject(error);
  },
);
