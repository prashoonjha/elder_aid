import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import * as authApi from '../api/auth';
import { fetchCurrentUser, type CurrentUser } from '../api/users';
import { setAccessToken, onSessionExpired, refreshAccessToken } from '../api/client';
import type { RegisterPayload, LoginPayload } from '../api/auth';

interface AuthContextValue {
  user: CurrentUser | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  register: (payload: RegisterPayload) => Promise<void>;
  login: (payload: LoginPayload) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<CurrentUser | null>(null);
  // Starts true so the app can show a loading state on first paint while we
  // check whether the httpOnly cookie can silently restore a session - this
  // is what makes a page reload keep you logged in instead of bouncing you
  // to the login screen.
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    onSessionExpired(() => {
      setUser(null);
    });
  }, []);

  // On first load, try to restore the session from the refresh cookie. If it
  // succeeds we get a fresh access token and fetch the user; if not, we land
  // logged out as normal - no error, just no session to restore.
  useEffect(() => {
    let cancelled = false;
    async function restoreSession() {
      const token = await refreshAccessToken();
      if (token && !cancelled) {
        try {
          const currentUser = await fetchCurrentUser();
          if (!cancelled) setUser(currentUser);
        } catch {
          // Access token worked but the user fetch failed - treat as no
          // session rather than a half-restored state.
        }
      }
      if (!cancelled) setIsLoading(false);
    }
    restoreSession();
    return () => {
      cancelled = true;
    };
  }, []);

  async function loadCurrentUser() {
    const currentUser = await fetchCurrentUser();
    setUser(currentUser);
  }

  async function register(payload: RegisterPayload) {
    const tokens = await authApi.register(payload);
    setAccessToken(tokens.accessToken);
    await loadCurrentUser();
  }

  async function login(payload: LoginPayload) {
    const tokens = await authApi.login(payload);
    setAccessToken(tokens.accessToken);
    await loadCurrentUser();
  }

  async function logout() {
    try {
      // Tell the backend to revoke the refresh token and clear the cookie.
      await authApi.logout();
    } catch {
      // Even if the network call fails, still clear local state below - the
      // user asked to log out, so honor that regardless.
    }
    setAccessToken(null);
    setUser(null);
  }

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isAuthenticated: user !== null,
      isLoading,
      register,
      login,
      logout,
    }),
    [user, isLoading],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
