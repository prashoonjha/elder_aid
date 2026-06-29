import { useParams, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useQuery, useMutation } from '@tanstack/react-query';
import { useState } from 'react';
import { isAxiosError } from 'axios';
import { ArrowLeft, Check } from 'lucide-react';
import { Button } from '../components/ui/Button';
import { getTaskSummary } from '../api/tasks';
import { applyToTask } from '../api/taskApplications';
import { TASK_CATEGORIES } from '../constants/taskCategories';

// Mirrors the client-side fee constant in TaskFormPage - both are
// placeholders until the real Payment feature makes the backend the
// source of truth for both sides of this calculation.
const WORKER_COMMISSION_RATE = 0.12;

/**
 * The backend doesn't return a machine-readable error code for these
 * cases, only a human-readable message - so we match on known substrings
 * rather than the exact string, which is a bit fragile but better than
 * showing raw English to a Finnish-language user. Falls back to a
 * translated generic message for anything unrecognized, rather than ever
 * surfacing the backend's own text directly.
 */
function mapApplyErrorToTranslationKey(error: unknown): string {
  if (isAxiosError<{ message?: string }>(error) && typeof error.response?.data?.message === 'string') {
    const message = error.response.data.message;
    if (message.includes('identity verification')) {
      return 'taskDetail.errors.notVerified';
    }
    if (message.includes('no longer accepting applications')) {
      return 'taskDetail.errors.taskClosed';
    }
    if (message.includes('already applied')) {
      return 'taskDetail.errors.alreadyApplied';
    }
  }
  return 'taskDetail.errors.generic';
}

export function TaskDetailPage() {
  const { taskId } = useParams<{ taskId: string }>();
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [hasApplied, setHasApplied] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const taskQuery = useQuery({
    queryKey: ['tasks', taskId],
    queryFn: () => getTaskSummary(taskId!),
    enabled: !!taskId,
  });

  const applyMutation = useMutation({
    mutationFn: () => applyToTask(taskId!, {}),
    onSuccess: () => setHasApplied(true),
    onError: (error) => {
      setErrorMessage(t(mapApplyErrorToTranslationKey(error)));
    },
  });

  if (taskQuery.isLoading || !taskQuery.data) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-white">
        <p className="text-sm text-brand-textSecondary">{t('common.loading')}</p>
      </main>
    );
  }

  const task = taskQuery.data;
  const categoryConfig = TASK_CATEGORIES.find((c) => c.value === task.category);
  const Icon = categoryConfig?.icon;
  const commission = task.priceOffered * WORKER_COMMISSION_RATE;
  const youEarn = task.priceOffered - commission;

  if (hasApplied) {
    return (
      <main className="flex min-h-screen flex-col items-center justify-center bg-white px-6 text-center">
        <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-brand-accentLight">
          <Check size={22} className="text-brand-accent" />
        </div>
        <p className="font-display text-base font-bold text-brand-primary">{t('taskDetail.applied.title')}</p>
        <p className="mt-1.5 max-w-xs text-sm text-brand-textSecondary">{t('taskDetail.applied.description')}</p>
        <div className="mt-6">
          <Button variant="secondary" fullWidth={false} onClick={() => navigate('/tasks')}>
            {t('taskDetail.applied.browseMore')}
          </Button>
        </div>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-white px-4 py-6">
      <div className="mx-auto max-w-md">
        <button onClick={() => navigate('/tasks')} aria-label={t('common.back')} className="mb-4 text-brand-textMuted">
          <ArrowLeft size={20} />
        </button>

        <div className="mb-1.5 flex items-center gap-1.5">
          {Icon && <Icon size={15} className="text-brand-accent" />}
          <span className="text-xs font-medium text-brand-accent">{categoryConfig && t(categoryConfig.labelKey)}</span>
        </div>
        <h1 className="font-display mb-3 text-lg font-bold text-brand-primary">
          {task.description || (categoryConfig && t(categoryConfig.labelKey))}
        </h1>

        <div className="mb-4 flex gap-5">
          <div>
            <p className="text-[10px] text-brand-textMuted">{t('taskDetail.when')}</p>
            <p className="text-xs text-brand-primary">{new Date(task.scheduledStart).toLocaleString()}</p>
          </div>
          <div>
            <p className="text-[10px] text-brand-textMuted">{t('taskDetail.where')}</p>
            <p className="text-xs text-brand-primary">{task.city}</p>
          </div>
        </div>

        {task.description && <p className="mb-4 text-sm text-brand-textSecondary">{task.description}</p>}

        <div className="mb-5 rounded-control border border-brand-border bg-brand-surface px-4 py-3">
          <div className="flex justify-between text-xs text-brand-textSecondary">
            <span>{t('taskDetail.breakdown.taskPrice')}</span>
            <span>{task.priceOffered.toFixed(2)} €</span>
          </div>
          <div className="mb-1.5 flex justify-between text-xs text-brand-textSecondary">
            <span>{t('taskDetail.breakdown.commission')}</span>
            <span>-{commission.toFixed(2)} €</span>
          </div>
          <div className="flex justify-between border-t border-brand-border pt-1.5 text-sm font-bold text-brand-accentDark">
            <span>{t('taskDetail.breakdown.youEarn')}</span>
            <span>{youEarn.toFixed(2)} €</span>
          </div>
        </div>

        {errorMessage && <p className="mb-4 text-sm text-red-600">{errorMessage}</p>}

        <Button onClick={() => applyMutation.mutate()} disabled={applyMutation.isPending}>
          {applyMutation.isPending ? t('taskDetail.applying') : t('taskDetail.apply')}
        </Button>
      </div>
    </main>
  );
}
