import { useParams, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { ArrowLeft, Star } from 'lucide-react';
import { Button } from '../components/ui/Button';
import { getMyTaskDetail } from '../api/tasks';
import { listApplicationsForTask, acceptApplication, rejectApplication } from '../api/taskApplications';
import { TASK_CATEGORIES } from '../constants/taskCategories';

function initialsOf(firstName: string, lastName: string): string {
  return `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase();
}

export function TaskApplicationsReviewPage() {
  const { taskId } = useParams<{ taskId: string }>();
  const { t } = useTranslation();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [actioningId, setActioningId] = useState<string | null>(null);

  const taskQuery = useQuery({
    queryKey: ['tasks', 'mine', taskId],
    queryFn: () => getMyTaskDetail(taskId!),
    enabled: !!taskId,
  });

  const applicationsQuery = useQuery({
    queryKey: ['applications', 'forTask', taskId],
    queryFn: () => listApplicationsForTask(taskId!),
    enabled: !!taskId,
  });

  function invalidateAfterAction() {
    queryClient.invalidateQueries({ queryKey: ['tasks', 'mine', taskId] });
    queryClient.invalidateQueries({ queryKey: ['applications', 'forTask', taskId] });
    setActioningId(null);
  }

  const acceptMutation = useMutation({
    mutationFn: (applicationId: string) => acceptApplication(taskId!, applicationId),
    onSuccess: invalidateAfterAction,
    onError: invalidateAfterAction,
  });

  const rejectMutation = useMutation({
    mutationFn: (applicationId: string) => rejectApplication(taskId!, applicationId),
    onSuccess: invalidateAfterAction,
    onError: invalidateAfterAction,
  });

  if (taskQuery.isLoading || !taskQuery.data) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-brand-surface">
        <p className="text-sm text-brand-textSecondary">{t('common.loading')}</p>
      </main>
    );
  }

  const task = taskQuery.data;
  const categoryConfig = TASK_CATEGORIES.find((c) => c.value === task.category);
  const Icon = categoryConfig?.icon;
  const canStillDecide = task.status === 'OPEN';

  return (
    <main className="flex min-h-screen items-center justify-center bg-brand-surface px-6 py-10">
      <div className="w-full max-w-lg rounded-card border border-brand-border bg-white p-9">
        <button onClick={() => navigate('/tasks/mine')} aria-label={t('common.back')} className="mb-4 text-brand-textMuted">
          <ArrowLeft size={20} />
        </button>

        <div className="mb-1 flex items-center gap-1.5">
          {Icon && <Icon size={14} className="text-brand-accent" />}
          <span className="text-xs font-medium text-brand-accent">{categoryConfig && t(categoryConfig.labelKey)}</span>
        </div>
        <h1 className="font-display mb-1 text-lg font-bold text-brand-primary">
          {task.description || (categoryConfig && t(categoryConfig.labelKey))}
        </h1>
        <p className="mb-6 text-xs text-brand-textSecondary">
          {new Date(task.scheduledStart).toLocaleString()} · {task.city} · {task.priceOffered.toFixed(2)} €
        </p>

        <p className="mb-2.5 text-xs font-medium text-brand-primary">
          {t('taskReview.applicationCount', { count: applicationsQuery.data?.length ?? 0 })}
        </p>

        {applicationsQuery.data && applicationsQuery.data.length === 0 && (
          <p className="text-sm text-brand-textSecondary">{t('taskReview.noApplications')}</p>
        )}

        <div className="flex flex-col gap-2.5">
          {applicationsQuery.data?.map((application) => {
            const isPending = application.status === 'PENDING';
            const isActioningThis = actioningId === application.id;
            return (
              <div key={application.id} className="rounded-card border border-brand-border p-3.5">
                <div className="flex gap-2.5">
                  <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-brand-accentLight">
                    <span className="text-xs font-bold text-brand-accentDark">
                      {initialsOf(application.workerFirstName, application.workerLastName)}
                    </span>
                  </div>
                  <div className="flex-1">
                    <div className="flex items-baseline justify-between">
                      <span className="text-sm font-semibold text-brand-primary">
                        {application.workerFirstName} {application.workerLastName}
                      </span>
                      <span className="flex items-center gap-0.5 text-xs text-brand-textSecondary">
                        <Star size={11} className="fill-amber-400 text-amber-400" />
                        {application.workerAverageRating.toFixed(1)}
                      </span>
                    </div>
                    {application.message && (
                      <p className="mt-1 text-xs text-brand-textSecondary">{application.message}</p>
                    )}
                  </div>
                </div>

                {canStillDecide && isPending ? (
                  <div className="mt-3 flex gap-2">
                    <Button
                      variant="secondary"
                      fullWidth
                      disabled={isActioningThis}
                      onClick={() => {
                        setActioningId(application.id);
                        rejectMutation.mutate(application.id);
                      }}
                    >
                      {t('taskReview.reject')}
                    </Button>
                    <Button
                      fullWidth
                      disabled={isActioningThis}
                      onClick={() => {
                        setActioningId(application.id);
                        acceptMutation.mutate(application.id);
                      }}
                    >
                      {t('taskReview.accept')}
                    </Button>
                  </div>
                ) : (
                  <p className="mt-3 text-xs font-medium text-brand-textMuted">
                    {t(`taskReview.applicationStatus.${application.status}`)}
                  </p>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </main>
  );
}
