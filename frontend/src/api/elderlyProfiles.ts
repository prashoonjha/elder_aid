import { apiClient } from './client';

export interface CreateElderlyProfilePayload {
  firstName: string;
  lastName: string;
  addressLine?: string;
  city?: string;
  postalCode?: string;
  preferredLanguage: 'fi' | 'en';
  relationship?: string;
  forSelf: boolean;
}

export interface ElderlyProfile {
  id: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string | null;
  addressLine: string | null;
  city: string | null;
  postalCode: string | null;
  preferredLanguage: string;
  relationship: string;
  permissionLevel: 'VIEW_ONLY' | 'CAN_BOOK' | 'FULL';
}

export async function createElderlyProfile(payload: CreateElderlyProfilePayload): Promise<ElderlyProfile> {
  const response = await apiClient.post<ElderlyProfile>('/api/elderly-profiles', payload);
  return response.data;
}

export async function listMyElderlyProfiles(): Promise<ElderlyProfile[]> {
  const response = await apiClient.get<ElderlyProfile[]>('/api/elderly-profiles/mine');
  return response.data;
}
