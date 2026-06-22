import { apiClient } from './client';
import type { UserRole } from './auth';

export interface CurrentUser {
  id: string;
  email: string;
  roles: UserRole[];
}

export async function fetchCurrentUser(): Promise<CurrentUser> {
  const response = await apiClient.get<CurrentUser>('/api/users/me');
  return response.data;
}
