import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import * as authApi from '../api/auth';
import { fetchCurrentUser, type CurrentUser } from '../api/users';
import { setTokens, onSessionExpired } from '../api/client';
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

  useEffect(() => {
    onSessionExpired(() => {
      setUser(null);
    });
  }, []);

  async function loadCurrentUser() {
    const currentUser = await fetchCurrentUser();
    setUser(currentUser);
  }

  async function register(payload: RegisterPayload) {
    const tokens = await authApi.register(payload);
    setTokens(tokens);
    await loadCurrentUser();
  }

  async function login(payload: LoginPayload) {
    const tokens = await authApi.login(payload);
    setTokens(tokens);
    await loadCurrentUser();
  }

  function logout() {
    setTokens(null);
    setUser(null);
  }

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isAuthenticated: user !== null,
      isLoading: false,
      register,
      login,
      logout,
    }),
    [user],
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
