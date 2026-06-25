import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useQuery } from '@tanstack/react-query';
import { browseOpenTasks, type TaskCategory } from '../api/tasks';
import { TASK_CATEGORIES } from '../constants/taskCategories';

export function TaskBrowsePage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [categoryFilter, setCategoryFilter] = useState<TaskCategory | 'ALL'>('ALL');

  const tasksQuery = useQuery({
    queryKey: ['tasks', 'open', categoryFilter],
    queryFn: () => browseOpenTasks(categoryFilter === 'ALL' ? undefined : categoryFilter),
  });

  return (
    <main className="min-h-screen bg-brand-surface px-4 py-6">
      <div className="mx-auto max-w-md">
        <h1 className="font-display mb-4 text-lg font-bold text-brand-primary">{t('taskBrowse.title')}</h1>

        <div className="mb-4 flex gap-2 overflow-x-auto pb-1">
          <button
            onClick={() => setCategoryFilter('ALL')}
            className={`shrink-0 rounded-full px-3 py-1.5 text-xs font-medium ${
              categoryFilter === 'ALL' ? 'bg-brand-accentLight text-brand-accentDark' : 'bg-white text-brand-textMuted'
            }`}
          >
            {t('taskBrowse.allCategories')}
          </button>
          {TASK_CATEGORIES.map(({ value, labelKey }) => (
            <button
              key={value}
              onClick={() => setCategoryFilter(value)}
              className={`shrink-0 rounded-full px-3 py-1.5 text-xs font-medium ${
                categoryFilter === value ? 'bg-brand-accentLight text-brand-accentDark' : 'bg-white text-brand-textMuted'
              }`}
            >
              {t(labelKey)}
            </button>
          ))}
        </div>

        {tasksQuery.isLoading && <p className="text-sm text-brand-textSecondary">{t('common.loading')}</p>}

        {tasksQuery.data && tasksQuery.data.content.length === 0 && (
          <p className="mt-8 text-center text-sm text-brand-textSecondary">{t('taskBrowse.empty')}</p>
        )}

        <div className="flex flex-col gap-2.5">
          {tasksQuery.data?.content.map((task) => {
            const categoryConfig = TASK_CATEGORIES.find((c) => c.value === task.category);
            const Icon = categoryConfig?.icon;
            return (
              <button
                key={task.id}
                onClick={() => navigate(`/tasks/${task.id}`)}
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
                  <span className="rounded-full bg-brand-accentLight px-2.5 py-1 text-[10px] font-medium text-brand-accentDark">
                    {t('taskBrowse.statusOpen')}
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
