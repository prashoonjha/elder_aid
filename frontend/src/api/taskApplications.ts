import { apiClient } from './client';

export interface ApplyToTaskPayload {
  message?: string;
}

export interface TaskApplication {
  id: string;
  taskRequestId: string;
  workerProfileId: string;
  status: string;
  message: string | null;
  appliedAt: string;
}

export async function applyToTask(taskId: string, payload: ApplyToTaskPayload): Promise<TaskApplication> {
  const response = await apiClient.post<TaskApplication>(`/api/tasks/${taskId}/applications`, payload);
  return response.data;
}
