import { apiClient } from './client';
import type { TaskCategory } from './tasks';

export interface BookingDetail {
  id: string;
  taskRequestId: string;
  workerProfileId: string;
  status: 'CONFIRMED' | 'CHECKED_IN' | 'COMPLETED' | 'CANCELLED' | 'DISPUTED';
  checkInTime: string | null;
  checkOutTime: string | null;
  createdAt: string;
  taskCategory: TaskCategory;
  taskCity: string | null;
  taskScheduledStart: string;
  taskPriceOffered: number;
}

export async function listMyBookings(): Promise<BookingDetail[]> {
  const response = await apiClient.get<BookingDetail[]>('/api/bookings/mine');
  return response.data;
}

export async function checkIn(bookingId: string): Promise<BookingDetail> {
  const response = await apiClient.patch<BookingDetail>(`/api/bookings/${bookingId}/check-in`);
  return response.data;
}

export async function checkOut(bookingId: string): Promise<BookingDetail> {
  const response = await apiClient.patch<BookingDetail>(`/api/bookings/${bookingId}/check-out`);
  return response.data;
}

export async function getBookingByTask(taskId: string): Promise<BookingDetail> {
  const response = await apiClient.get<BookingDetail>(`/api/tasks/${taskId}/booking`);
  return response.data;
}
