import { apiClient } from './client';
import type { UserRole } from './auth';

export type VerificationTier = 'NONE' | 'TIER1_ID_VERIFIED' | 'TIER2_BACKGROUND_CHECKED';

export interface CurrentUser {
  id: string;
  email: string;
  firstName: string;
  roles: UserRole[];
  verificationTier: VerificationTier | null;
}

export async function fetchCurrentUser(): Promise<CurrentUser> {
  const response = await apiClient.get<CurrentUser>('/api/users/me');
  return response.data;
}

export async function exportMyData(): Promise<unknown> {
  const response = await apiClient.get('/api/users/me/data-export');
  return response.data;
}

export async function deleteMyAccount(password: string): Promise<void> {
  await apiClient.delete('/api/users/me', { data: { password } });
}
