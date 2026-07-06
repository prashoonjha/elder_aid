import { apiClient } from './client';

export interface ApplyToTaskPayload {
  message?: string;
}

export interface TaskApplication {
  id: string;
  taskRequestId: string;
  workerProfileId: string;
  workerFirstName: string;
  workerLastName: string;
  workerAverageRating: number;
  workerReviewCount: number;
  status: string;
  message: string | null;
  appliedAt: string;
}

export interface Booking {
  id: string;
  taskRequestId: string;
  workerProfileId: string;
  status: string;
  checkInTime: string | null;
  checkOutTime: string | null;
  createdAt: string;
}

export async function applyToTask(taskId: string, payload: ApplyToTaskPayload): Promise<TaskApplication> {
  const response = await apiClient.post<TaskApplication>(`/api/tasks/${taskId}/applications`, payload);
  return response.data;
}

export async function listApplicationsForTask(taskId: string): Promise<TaskApplication[]> {
  const response = await apiClient.get<TaskApplication[]>(`/api/tasks/${taskId}/applications`);
  return response.data;
}

export async function acceptApplication(taskId: string, applicationId: string): Promise<Booking> {
  const response = await apiClient.patch<Booking>(`/api/tasks/${taskId}/applications/${applicationId}/accept`);
  return response.data;
}

export async function rejectApplication(taskId: string, applicationId: string): Promise<TaskApplication> {
  const response = await apiClient.patch<TaskApplication>(`/api/tasks/${taskId}/applications/${applicationId}/reject`);
  return response.data;
}
