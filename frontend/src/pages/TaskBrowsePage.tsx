import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useQuery } from '@tanstack/react-query';
import { browseOpenTasks, type TaskCategory, type TaskSummary } from '../api/tasks';
import { TASK_CATEGORIES } from '../constants/taskCategories';

export function TaskBrowsePage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [categoryFilter, setCategoryFilter] = useState<TaskCategory | 'ALL'>('ALL');
  const [page, setPage] = useState(0);
  const [allTasks, setAllTasks] = useState<TaskSummary[]>([]);
  const [isLastPage, setIsLastPage] = useState(false);

  const tasksQuery = useQuery({
    queryKey: ['tasks', 'open', categoryFilter, page],
    queryFn: () => browseOpenTasks(categoryFilter === 'ALL' ? undefined : categoryFilter, page),
    onSuccess: (data) => {
      // On a category change (page resets to 0) replace the list entirely;
      // on a "load more" click (page > 0) append to what's already shown.
      setAllTasks((prev) => page === 0 ? data.content : [...prev, ...data.content]);
      setIsLastPage(data.last);
    },
  });

  function handleCategoryChange(value: TaskCategory | 'ALL') {
    setCategoryFilter(value);
    setPage(0);
    setAllTasks([]);
    setIsLastPage(false);
  }

  return (
    <main className="min-h-screen bg-brand-surface px-4 py-6">
      <div className="mx-auto max-w-md">
        <h1 className="font-display mb-4 text-lg font-bold text-brand-primary">{t('taskBrowse.title')}</h1>

        <div className="mb-4 flex gap-2 overflow-x-auto pb-1">
          <button
            onClick={() => handleCategoryChange('ALL')}
            className={`shrink-0 rounded-full px-3 py-1.5 text-xs font-medium ${
              categoryFilter === 'ALL' ? 'bg-brand-accentLight text-brand-accentDark' : 'bg-white text-brand-textMuted'
            }`}
          >
            {t('taskBrowse.allCategories')}
          </button>
          {TASK_CATEGORIES.map(({ value, labelKey }) => (
            <button
              key={value}
              onClick={() => handleCategoryChange(value)}
              className={`shrink-0 rounded-full px-3 py-1.5 text-xs font-medium ${
                categoryFilter === value ? 'bg-brand-accentLight text-brand-accentDark' : 'bg-white text-brand-textMuted'
              }`}
            >
              {t(labelKey)}
            </button>
          ))}
        </div>

        {tasksQuery.isLoading && page === 0 && (
          <p className="text-sm text-brand-textSecondary">{t('common.loading')}</p>
        )}

        {allTasks.length === 0 && !tasksQuery.isLoading && (
          <p className="mt-8 text-center text-sm text-brand-textSecondary">{t('taskBrowse.empty')}</p>
        )}

        <div className="flex flex-col gap-2.5">
          {allTasks.map((task) => {
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

        {!isLastPage && allTasks.length > 0 && (
          <button
            onClick={() => setPage((p) => p + 1)}
            disabled={tasksQuery.isLoading}
            className="mt-4 w-full rounded-control border border-brand-border bg-white py-2.5 text-sm font-medium text-brand-primary disabled:opacity-50"
          >
            {tasksQuery.isLoading ? t('common.loading') : t('taskBrowse.loadMore')}
          </button>
        )}
      </div>
    </main>
  );
}
