import { apiClient } from './client';
import type { DocumentType, DocumentStatus } from './verification';

export interface AdminVerificationDocument {
  id: string;
  workerProfileId: string;
  workerFirstName: string;
  workerLastName: string;
  workerEmail: string;
  documentType: DocumentType;
  status: DocumentStatus;
  submittedAt: string;
}

export async function listPendingDocuments(): Promise<AdminVerificationDocument[]> {
  const response = await apiClient.get<AdminVerificationDocument[]>('/api/admin/verification-documents');
  return response.data;
}

export async function approveDocument(documentId: string): Promise<void> {
  await apiClient.post(`/api/admin/verification-documents/${documentId}/approve`);
}

export async function rejectDocument(documentId: string, reason: string): Promise<void> {
  await apiClient.post(`/api/admin/verification-documents/${documentId}/reject`, { reason });
}
