import { useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft, ShieldCheck, CheckCircle, Clock, XCircle, Upload } from 'lucide-react';
import { listMyDocuments, uploadDocument, type DocumentType, type VerificationDocument } from '../api/verification';

const ACCEPTED = ['image/jpeg', 'image/png', 'application/pdf'];
const MAX_BYTES = 10 * 1024 * 1024;
const DOC_TYPES: DocumentType[] = ['ID_CARD', 'SELFIE'];

export function VerificationPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);

  const docsQuery = useQuery({
    queryKey: ['verification', 'documents'],
    queryFn: listMyDocuments,
  });

  const uploadMutation = useMutation({
    mutationFn: ({ type, file }: { type: DocumentType; file: File }) => uploadDocument(type, file),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['verification', 'documents'] });
      queryClient.invalidateQueries({ queryKey: ['currentUser'] });
    },
    onError: () => setError(t('verification.uploadError')),
  });

  const documentsByType = (docsQuery.data ?? []).reduce<Record<string, VerificationDocument>>((acc, doc) => {
    // Keep the most recent submission per type.
    const existing = acc[doc.documentType];
    if (!existing || new Date(doc.submittedAt) > new Date(existing.submittedAt)) {
      acc[doc.documentType] = doc;
    }
    return acc;
  }, {});

  const approvedCount = DOC_TYPES.filter((type) => documentsByType[type]?.status === 'APPROVED').length;
  const isVerified = approvedCount === DOC_TYPES.length;

  function handleFile(type: DocumentType, fileList: FileList | null) {
    setError(null);
    const file = fileList?.[0];
    if (!file) return;
    // Client-side checks that mirror the backend, so the user gets instant feedback.
    if (!ACCEPTED.includes(file.type)) {
      setError(t('verification.errorType'));
      return;
    }
    if (file.size > MAX_BYTES) {
      setError(t('verification.errorSize'));
      return;
    }
    uploadMutation.mutate({ type, file });
  }

  return (
    <main className="min-h-screen bg-brand-surface px-5 py-6">
      <div className="mx-auto max-w-lg">
        <button
          onClick={() => navigate('/dashboard')}
          aria-label={t('common.back')}
          className="mb-4 flex items-center gap-1.5 text-sm text-brand-textSecondary hover:text-brand-primary"
        >
          <ArrowLeft size={18} />
          {t('common.back')}
        </button>

        <h1 className="font-display mb-1 text-2xl font-bold text-brand-primary">{t('verification.title')}</h1>
        <p className="mb-5 text-base text-brand-textSecondary">{t('verification.subtitle')}</p>

        <div className={`mb-6 flex items-center justify-between rounded-card p-4 ${isVerified ? 'bg-brand-accentLight' : 'bg-brand-primary'}`}>
          <div className="flex items-center gap-2.5">
            <ShieldCheck size={22} className={isVerified ? 'text-brand-accentDark' : 'text-white'} />
            <div>
              <p className={`text-sm font-semibold ${isVerified ? 'text-brand-accentDark' : 'text-white'}`}>
                {isVerified ? t('verification.statusVerified') : t('verification.statusUnverified')}
              </p>
              <p className={`text-xs ${isVerified ? 'text-brand-accentDark/80' : 'text-white/70'}`}>
                {t('verification.progress', { count: approvedCount, total: DOC_TYPES.length })}
              </p>
            </div>
          </div>
        </div>

        {error && <p className="mb-4 rounded-field bg-red-50 px-4 py-3 text-sm text-red-700">{error}</p>}

        <div className="space-y-3">
          {DOC_TYPES.map((type) => (
            <DocumentSlot
              key={type}
              type={type}
              document={documentsByType[type]}
              uploading={uploadMutation.isPending && uploadMutation.variables?.type === type}
              onFile={(files) => handleFile(type, files)}
            />
          ))}
        </div>
      </div>
    </main>
  );
}

interface DocumentSlotProps {
  type: DocumentType;
  document?: VerificationDocument;
  uploading: boolean;
  onFile: (files: FileList | null) => void;
}

function DocumentSlot({ type, document, uploading, onFile }: DocumentSlotProps) {
  const { t } = useTranslation();
  const inputRef = useRef<HTMLInputElement>(null);
  const status = document?.status;

  const badge =
    status === 'APPROVED'
      ? { Icon: CheckCircle, className: 'bg-brand-accentLight text-brand-accentDark', label: t('verification.approved') }
      : status === 'PENDING'
      ? { Icon: Clock, className: 'bg-amber-50 text-amber-700', label: t('verification.pending') }
      : status === 'REJECTED'
      ? { Icon: XCircle, className: 'bg-red-50 text-red-700', label: t('verification.rejected') }
      : null;

  // Upload allowed when there's no document yet, or the last one was rejected.
  const canUpload = !document || status === 'REJECTED';

  return (
    <div className="rounded-card bg-white p-4 shadow-card">
      <div className="mb-2 flex items-center justify-between">
        <p className="text-base font-semibold text-brand-primary">{t(`verification.docTypes.${type}`)}</p>
        {badge && (
          <span className={`flex items-center gap-1 rounded-full px-2.5 py-1 text-xs font-medium ${badge.className}`}>
            <badge.Icon size={13} />
            {badge.label}
          </span>
        )}
      </div>

      {status === 'REJECTED' && document?.rejectionReason && (
        <p className="mb-3 text-sm text-red-600">{t('verification.rejectedReason', { reason: document.rejectionReason })}</p>
      )}

      {canUpload ? (
        <>
          <input
            ref={inputRef}
            type="file"
            accept="image/jpeg,image/png,application/pdf"
            className="hidden"
            onChange={(e) => onFile(e.target.files)}
          />
          <button
            onClick={() => inputRef.current?.click()}
            disabled={uploading}
            className="flex w-full flex-col items-center gap-1.5 rounded-field border-[1.5px] border-dashed border-brand-border py-5 text-brand-accent hover:border-brand-accent disabled:opacity-50"
          >
            <Upload size={20} />
            <span className="text-sm font-medium">
              {uploading ? t('verification.uploading') : status === 'REJECTED' ? t('verification.reupload') : t('verification.upload')}
            </span>
            <span className="text-xs text-brand-textMuted">{t('verification.fileHint')}</span>
          </button>
        </>
      ) : (
        <p className="text-sm text-brand-textSecondary">
          {status === 'APPROVED' ? t('verification.approvedNote') : t('verification.pendingNote')}
        </p>
      )}
    </div>
  );
}
