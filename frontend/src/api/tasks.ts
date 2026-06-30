import { apiClient } from './client';

export type TaskCategory =
  | 'GROCERY_SHOPPING'
  | 'WALKING_COMPANION'
  | 'CHATTING_COMPANIONSHIP'
  | 'HOUSEHOLD_HELP'
  | 'OTHER';

export interface CreateTaskPayload {
  elderlyProfileId: string;
  category: TaskCategory;
  description?: string;
  addressLine?: string;
  city?: string;
  scheduledStart: string; // ISO 8601, including offset
  scheduledEnd: string;
  priceOffered: number;
}

export interface TaskDetail {
  id: string;
  elderlyProfileId: string;
  category: TaskCategory;
  description: string | null;
  addressLine: string | null;
  city: string | null;
  scheduledStart: string;
  scheduledEnd: string;
  priceOffered: number;
  status: string;
  createdAt: string;
}

export interface TaskSummary {
  id: string;
  category: TaskCategory;
  description: string | null;
  city: string | null;
  scheduledStart: string;
  scheduledEnd: string;
  priceOffered: number;
  status: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  last: boolean;
}

export async function browseOpenTasks(category?: TaskCategory, page = 0): Promise<PageResponse<TaskSummary>> {
  const response = await apiClient.get<PageResponse<TaskSummary>>('/api/tasks', {
    params: {
      ...(category ? { category } : {}),
      page,
      size: 10,
    },
  });
  return response.data;
}

export async function getTaskSummary(taskId: string): Promise<TaskSummary> {
  const response = await apiClient.get<TaskSummary>(`/api/tasks/${taskId}`);
  return response.data;
}

export async function createTask(payload: CreateTaskPayload): Promise<TaskDetail> {
  const response = await apiClient.post<TaskDetail>('/api/tasks', payload);
  return response.data;
}

export async function getMyTaskDetail(taskId: string): Promise<TaskDetail> {
  const response = await apiClient.get<TaskDetail>(`/api/tasks/mine/${taskId}`);
  return response.data;
}

export async function listMyTasks(): Promise<TaskDetail[]> {
  const response = await apiClient.get<TaskDetail[]>('/api/tasks/mine');
  return response.data;
}
