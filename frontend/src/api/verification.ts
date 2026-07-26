import { apiClient } from './client';

export type DocumentType = 'ID_CARD' | 'SELFIE';
export type DocumentStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface VerificationDocument {
  id: string;
  documentType: DocumentType;
  status: DocumentStatus;
  submittedAt: string;
  reviewedAt: string | null;
  rejectionReason: string | null;
}

export async function listMyDocuments(): Promise<VerificationDocument[]> {
  const response = await apiClient.get<VerificationDocument[]>('/api/workers/me/verification-documents');
  return response.data;
}

export async function uploadDocument(documentType: DocumentType, file: File): Promise<VerificationDocument> {
  const form = new FormData();
  form.append('documentType', documentType);
  form.append('file', file);
  const response = await apiClient.post<VerificationDocument>('/api/workers/me/verification-documents', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return response.data;
}
