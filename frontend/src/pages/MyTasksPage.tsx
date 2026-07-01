import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useQuery } from '@tanstack/react-query';
import { listMyTasks } from '../api/tasks';
import { TASK_CATEGORIES } from '../constants/taskCategories';

const STATUS_STYLES: Record<string, string> = {
  OPEN: 'bg-brand-accentLight text-brand-accentDark',
  MATCHED: 'bg-brand-primary text-white',
  COMPLETED: 'bg-brand-accentLight text-brand-accentDark',
  CANCELLED: 'bg-brand-surface text-brand-textMuted',
};

export function MyTasksPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const tasksQuery = useQuery({
    queryKey: ['tasks', 'mine'],
    queryFn: listMyTasks,
  });

  return (
    <main className="min-h-screen bg-brand-surface px-4 py-6">
      <div className="mx-auto max-w-md">
        <h1 className="font-display mb-4 text-lg font-bold text-brand-primary">{t('myTasks.title')}</h1>

        {tasksQuery.isLoading && <p className="text-sm text-brand-textSecondary">{t('common.loading')}</p>}

        {tasksQuery.data && tasksQuery.data.length === 0 && (
          <p className="mt-8 text-center text-sm text-brand-textSecondary">{t('myTasks.empty')}</p>
        )}

        <div className="flex flex-col gap-2.5">
          {tasksQuery.data?.map((task) => {
            const categoryConfig = TASK_CATEGORIES.find((c) => c.value === task.category);
            const Icon = categoryConfig?.icon;
            return (
              <button
                key={task.id}
                onClick={() =>
                  task.status === 'OPEN'
                    ? navigate(`/tasks/mine/${task.id}/review`)
                    : navigate(`/tasks/mine/${task.id}/booking`)
                }
                className="rounded-card border border-brand-border bg-white p-3 text-left"
              >
                <div className="mb-1.5 flex items-center gap-1.5">
                  {Icon && <Icon size={15} className="text-brand-accent" />}
                  <span className="text-xs font-medium text-brand-primary">{categoryConfig && t(categoryConfig.labelKey)}</span>
                </div>
                <p className="mb-2 text-xs text-brand-textMuted">
                  {task.city} · {new Date(task.scheduledStart).toLocaleString()}
                </p>
                <div className="flex items-center justify-between">
                  <span className="text-sm font-bold text-brand-primary">{task.priceOffered.toFixed(2)} €</span>
                  <span className={`rounded-full px-2.5 py-1 text-[10px] font-medium ${STATUS_STYLES[task.status] ?? ''}`}>
                    {t(`myTasks.status.${task.status}`)}
                  </span>
                </div>
              </button>
            );
          })}
        </div>
      </div>
    </main>
  );
}
