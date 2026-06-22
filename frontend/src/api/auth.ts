import { apiClient } from './client';

export type UserRole = 'CLIENT' | 'FAMILY_MEMBER' | 'WORKER';

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  expiresInSeconds: number;
}

export interface RegisterPayload {
  email: string;
  firstName: string;
  lastName: string;
  password: string;
  phone?: string;
  role: UserRole;
  termsAccepted: boolean;
  locale: 'fi' | 'en';
}

export interface LoginPayload {
  email: string;
  password: string;
}

export async function register(payload: RegisterPayload): Promise<AuthTokens> {
  const response = await apiClient.post<AuthTokens>('/api/auth/register', payload);
  return response.data;
}

export async function login(payload: LoginPayload): Promise<AuthTokens> {
  const response = await apiClient.post<AuthTokens>('/api/auth/login', payload);
  return response.data;
}

export async function logout(refreshToken: string): Promise<void> {
  await apiClient.post('/api/auth/logout', { refreshToken });
}
