import { apiClient } from './client';

export interface Review {
  id: string;
  bookingId: string;
  ratedUserId: string;
  rating: number;
  comment: string | null;
  createdAt: string;
}

export interface CreateReviewPayload {
  rating: number;
  comment?: string;
}

export async function submitReview(bookingId: string, payload: CreateReviewPayload): Promise<Review> {
  const response = await apiClient.post<Review>(`/api/bookings/${bookingId}/review`, payload);
  return response.data;
}
