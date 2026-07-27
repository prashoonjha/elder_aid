import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { CheckCircle, XCircle, ShieldCheck } from 'lucide-react';
import { listPendingDocuments, approveDocument, rejectDocument, type AdminVerificationDocument } from '../api/adminVerification';

export function AdminVerificationPage() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const [rejectingId, setRejectingId] = useState<string | null>(null);
  const [reason, setReason] = useState('');

  const queueQuery = useQuery({
    queryKey: ['admin', 'verification', 'pending'],
    queryFn: listPendingDocuments,
  });

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['admin', 'verification', 'pending'] });

  const approveMutation = useMutation({
    mutationFn: (id: string) => approveDocument(id),
    onSuccess: invalidate,
  });

  const rejectMutation = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) => rejectDocument(id, reason),
    onSuccess: () => {
      setRejectingId(null);
      setReason('');
      invalidate();
    },
  });

  const documents = queueQuery.data ?? [];

  return (
    <main className="min-h-screen bg-brand-surface px-5 py-6">
      <div className="mx-auto max-w-3xl">
        <div className="mb-5 flex items-center gap-2">
          <ShieldCheck size={24} className="text-brand-accent" />
          <h1 className="font-display text-2xl font-bold text-brand-primary">{t('admin.verification.title')}</h1>
        </div>

        {queueQuery.isLoading && <p className="text-sm text-brand-textSecondary">{t('common.loading')}</p>}

        {!queueQuery.isLoading && documents.length === 0 && (
          <div className="rounded-card bg-white p-8 text-center shadow-card">
            <CheckCircle size={36} className="mx-auto mb-3 text-brand-accent" />
            <p className="text-base font-semibold text-brand-primary">{t('admin.verification.emptyTitle')}</p>
            <p className="mt-1 text-sm text-brand-textSecondary">{t('admin.verification.emptyDescription')}</p>
          </div>
        )}

        <div className="space-y-3">
          {documents.map((doc: AdminVerificationDocument) => (
            <div key={doc.id} className="rounded-card bg-white p-4 shadow-card">
              <div className="mb-3 flex items-start justify-between">
                <div>
                  <p className="text-base font-semibold text-brand-primary">
                    {doc.workerFirstName} {doc.workerLastName}
                  </p>
                  <p className="text-sm text-brand-textSecondary">{doc.workerEmail}</p>
                  <p className="mt-1 text-sm text-brand-textMuted">
                    {t(`verification.docTypes.${doc.documentType}`)} · {new Date(doc.submittedAt).toLocaleString()}
                  </p>
                </div>
              </div>

              {rejectingId === doc.id ? (
                <div>
                  <textarea
                    value={reason}
                    onChange={(e) => setReason(e.target.value)}
                    placeholder={t('admin.verification.reasonPlaceholder')}
                    rows={2}
                    className="mb-2 w-full rounded-field border border-brand-border bg-brand-surface px-3 py-2 text-sm text-brand-primary focus:outline-none focus:ring-2 focus:ring-brand-accent"
                  />
                  <div className="flex gap-2">
                    <button
                      onClick={() => rejectMutation.mutate({ id: doc.id, reason })}
                      disabled={!reason.trim() || rejectMutation.isPending}
                      className="rounded-control bg-red-600 px-4 py-2 text-sm font-semibold text-white disabled:opacity-50"
                    >
                      {t('admin.verification.confirmReject')}
                    </button>
                    <button
                      onClick={() => { setRejectingId(null); setReason(''); }}
                      className="rounded-control border border-brand-border px-4 py-2 text-sm font-medium text-brand-primary"
                    >
                      {t('common.cancel')}
                    </button>
                  </div>
                </div>
              ) : (
                <div className="flex gap-2">
                  <button
                    onClick={() => approveMutation.mutate(doc.id)}
                    disabled={approveMutation.isPending}
                    className="flex items-center gap-1.5 rounded-control bg-brand-accent px-4 py-2 text-sm font-semibold text-white disabled:opacity-50"
                  >
                    <CheckCircle size={16} />
                    {t('admin.verification.approve')}
                  </button>
                  <button
                    onClick={() => setRejectingId(doc.id)}
                    className="flex items-center gap-1.5 rounded-control border border-brand-border px-4 py-2 text-sm font-medium text-brand-primary"
                  >
                    <XCircle size={16} />
                    {t('admin.verification.reject')}
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      </div>
    </main>
  );
}
