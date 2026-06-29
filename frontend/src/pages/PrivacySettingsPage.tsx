import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useMutation } from '@tanstack/react-query';
import { isAxiosError } from 'axios';
import { TextField } from '../components/ui/TextField';
import { Button } from '../components/ui/Button';
import { useAuth } from '../context/AuthContext';
import { exportMyData, deleteMyAccount } from '../api/users';

export function PrivacySettingsPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { logout } = useAuth();

  const [password, setPassword] = useState('');
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const exportMutation = useMutation({
    mutationFn: exportMyData,
    onSuccess: (data) => {
      // Triggering a browser download from data we already have in memory,
      // rather than pointing the browser straight at the API URL - the
      // request needs the Authorization header, which a plain link click
      // can't attach, so this has to go through our own apiClient first.
      const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'elderaid-data-export.json';
      link.click();
      URL.revokeObjectURL(url);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: () => deleteMyAccount(password),
    onSuccess: () => {
      logout();
      navigate('/', { replace: true });
    },
    onError: (error) => {
      if (isAxiosError(error) && error.response?.status === 401) {
        setDeleteError(t('privacy.errors.wrongPassword'));
      } else {
        setDeleteError(t('privacy.errors.generic'));
      }
    },
  });

  function handleDeleteSubmit(event: FormEvent) {
    event.preventDefault();
    setDeleteError(null);
    deleteMutation.mutate();
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-brand-surface px-6 py-10">
      <div className="w-full max-w-lg rounded-card border border-brand-border bg-white p-9">
        <h1 className="font-display mb-5 text-lg font-bold text-brand-primary">{t('privacy.title')}</h1>

        <p className="mb-1 text-xs font-semibold text-brand-primary">{t('privacy.export.heading')}</p>
        <p className="mb-3 text-xs text-brand-textSecondary">{t('privacy.export.description')}</p>
        <Button variant="secondary" fullWidth={false} onClick={() => exportMutation.mutate()} disabled={exportMutation.isPending}>
          {exportMutation.isPending ? t('privacy.export.downloading') : t('privacy.export.cta')}
        </Button>

        <hr className="my-6 border-brand-border" />

        <p className="mb-1 text-xs font-semibold text-red-700">{t('privacy.delete.heading')}</p>
        <p className="mb-4 text-xs text-brand-textSecondary">{t('privacy.delete.description')}</p>

        <form onSubmit={handleDeleteSubmit}>
          <TextField
            label={t('privacy.delete.passwordLabel')}
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />

          {deleteError && <p className="mb-4 text-sm text-red-600">{deleteError}</p>}

          <button
            type="submit"
            disabled={deleteMutation.isPending}
            className="rounded-control bg-red-700 px-5 py-2.5 text-sm font-medium text-white transition-colors hover:bg-red-800 disabled:opacity-50"
          >
            {deleteMutation.isPending ? t('privacy.delete.submitting') : t('privacy.delete.cta')}
          </button>
        </form>
      </div>
    </main>
  );
}
