import { useEffect, useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useQuery, useMutation } from '@tanstack/react-query';
import { isAxiosError } from 'axios';
import { TextField } from '../components/ui/TextField';
import { Button } from '../components/ui/Button';
import { listMyElderlyProfiles } from '../api/elderlyProfiles';
import { createTask, type TaskCategory } from '../api/tasks';
import { TASK_CATEGORIES } from '../constants/taskCategories';
import { calculateClientBreakdown, calculateWorkerPayout } from '../lib/pricing';

export function TaskFormPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const profilesQuery = useQuery({
    queryKey: ['elderlyProfiles', 'mine'],
    queryFn: listMyElderlyProfiles,
  });

  const [elderlyProfileId, setElderlyProfileId] = useState('');
  const [category, setCategory] = useState<TaskCategory>('GROCERY_SHOPPING');
  const [description, setDescription] = useState('');
  const [scheduledStartLocal, setScheduledStartLocal] = useState('');
  const [durationHours, setDurationHours] = useState('1');
  const [addressLine, setAddressLine] = useState('');
  const [city, setCity] = useState('');
  const [priceOffered, setPriceOffered] = useState('');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // Default to the first profile once profiles actually load - can't do
  // this at useState init time since the data doesn't exist yet then.
  useEffect(() => {
    if (profilesQuery.data && profilesQuery.data.length > 0 && !elderlyProfileId) {
      setElderlyProfileId(profilesQuery.data[0].id);
    }
  }, [profilesQuery.data, elderlyProfileId]);

  const createTaskMutation = useMutation({
    mutationFn: createTask,
    onSuccess: () => navigate('/dashboard', { replace: true }),
    onError: (error) => {
      if (isAxiosError(error) && error.response?.status === 403) {
        setErrorMessage(t('newTask.errors.forbidden'));
      } else if (isAxiosError(error) && (error.response?.status === 400 || error.response?.status === 404)) {
        setErrorMessage(t('newTask.errors.validation'));
      } else {
        setErrorMessage(t('newTask.errors.generic'));
      }
    },
  });

  const { price, serviceFee, total } = calculateClientBreakdown(parseFloat(priceOffered));
  const workerPayout = calculateWorkerPayout(price);

  function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setErrorMessage(null);

    if (!scheduledStartLocal) {
      setErrorMessage(t('newTask.errors.validation'));
      return;
    }

    const scheduledStart = new Date(scheduledStartLocal);
    const scheduledEnd = new Date(scheduledStart.getTime() + parseFloat(durationHours || '0') * 60 * 60 * 1000);

    createTaskMutation.mutate({
      elderlyProfileId,
      category,
      description,
      addressLine,
      city,
      scheduledStart: scheduledStart.toISOString(),
      scheduledEnd: scheduledEnd.toISOString(),
      priceOffered: price,
    });
  }

  if (profilesQuery.isLoading) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-brand-surface">
        <p className="text-sm text-brand-textSecondary">{t('common.loading')}</p>
      </main>
    );
  }

  // No profile to post for yet - a disabled form would be more confusing
  // than helpful, so send them straight back to create one.
  if (profilesQuery.data && profilesQuery.data.length === 0) {
    return (
      <main className="flex min-h-screen flex-col items-center justify-center bg-brand-surface px-6 text-center">
        <div className="max-w-sm rounded-card border border-brand-border bg-white p-8">
          <p className="font-display text-lg font-bold text-brand-primary">{t('newTask.emptyState.title')}</p>
          <p className="mt-2 text-sm text-brand-textSecondary">{t('newTask.emptyState.description')}</p>
          <div className="mt-6">
            <Button onClick={() => navigate('/profiles/new')}>{t('newTask.emptyState.cta')}</Button>
          </div>
        </div>
      </main>
    );
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-brand-surface px-6 py-10">
      <div className="w-full max-w-xl rounded-card border border-brand-border bg-white p-9">
        <h1 className="font-display mb-5 text-lg font-bold text-brand-primary">{t('newTask.title')}</h1>

        <form onSubmit={handleSubmit}>
          <label className="mb-1 block text-xs text-brand-textSecondary" htmlFor="profile-select">
            {t('newTask.forWhom')}
          </label>
          <select
            id="profile-select"
            value={elderlyProfileId}
            onChange={(e) => setElderlyProfileId(e.target.value)}
            className="mb-4 w-full rounded-field bg-brand-surface px-3 py-2.5 text-sm text-slate-900 outline-none focus:ring-2 focus:ring-brand-accent"
          >
            {profilesQuery.data?.map((profile) => (
              <option key={profile.id} value={profile.id}>
                {profile.firstName} {profile.lastName}
              </option>
            ))}
          </select>

          <p className="mb-2 block text-xs text-brand-textSecondary">{t('newTask.category')}</p>
          <div className="mb-4 flex gap-2">
            {TASK_CATEGORIES.map(({ value, icon: Icon, labelKey }) => {
              const isSelected = value === category;
              return (
                <button
                  key={value}
                  type="button"
                  onClick={() => setCategory(value)}
                  className={`flex-1 rounded-field border py-2 text-center transition-colors ${
                    isSelected ? 'border-2 border-brand-accent bg-brand-accentLight' : 'border-brand-border'
                  }`}
                >
                  <Icon size={16} className={isSelected ? 'mx-auto text-brand-accentDark' : 'mx-auto text-brand-primary'} />
                  <p className={`mt-1 text-[10px] font-medium ${isSelected ? 'text-brand-accentDark' : 'text-brand-primary'}`}>
                    {t(labelKey)}
                  </p>
                </button>
              );
            })}
          </div>

          <div className="mb-3">
            <label className="mb-1 block text-xs text-brand-textSecondary" htmlFor="description">
              {t('newTask.description')}
            </label>
            <textarea
              id="description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={2}
              className="w-full rounded-field bg-brand-surface px-3 py-2.5 text-sm text-slate-900 outline-none focus:ring-2 focus:ring-brand-accent"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <TextField
              label={t('newTask.scheduledStart')}
              type="datetime-local"
              value={scheduledStartLocal}
              onChange={(e) => setScheduledStartLocal(e.target.value)}
              required
            />
            <TextField
              label={t('newTask.durationHours')}
              type="number"
              min="0.5"
              step="0.5"
              value={durationHours}
              onChange={(e) => setDurationHours(e.target.value)}
              required
            />
          </div>

          <div className="grid grid-cols-3 gap-3">
            <div className="col-span-2">
              <TextField label={t('newTask.address')} value={addressLine} onChange={(e) => setAddressLine(e.target.value)} />
            </div>
            <TextField label={t('newTask.city')} value={city} onChange={(e) => setCity(e.target.value)} />
          </div>

          <TextField
            label={t('newTask.price')}
            type="number"
            min="1"
            step="0.5"
            value={priceOffered}
            onChange={(e) => setPriceOffered(e.target.value)}
            required
          />

          {price > 0 && (
            <div className="mb-5 rounded-control border border-brand-border bg-brand-surface px-4 py-3">
              <div className="flex justify-between text-xs text-brand-textSecondary">
                <span>{t('newTask.breakdown.task')}</span>
                <span>{price.toFixed(2)} €</span>
              </div>
              <div className="mb-1.5 flex justify-between text-xs text-brand-textSecondary">
                <span>{t('newTask.breakdown.serviceFee')}</span>
                <span>{serviceFee.toFixed(2)} €</span>
              </div>
              <div className="flex justify-between border-t border-brand-border pt-1.5 text-sm font-bold text-brand-primary">
                <span>{t('newTask.breakdown.total')}</span>
                <span>{total.toFixed(2)} €</span>
              </div>
              {/* Not part of what the client pays - shown so they can see the
                  split rather than wondering where the commission goes. */}
              <p className="mt-2 border-t border-brand-border pt-2 text-xs text-brand-textMuted">
                {t('newTask.breakdown.workerReceives', { amount: workerPayout.toFixed(2) })}
              </p>
            </div>
          )}

          {errorMessage && <p className="mb-4 text-sm text-red-600">{errorMessage}</p>}

          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" fullWidth={false} onClick={() => navigate('/dashboard')}>
              {t('newTask.cancel')}
            </Button>
            <Button type="submit" fullWidth={false} disabled={createTaskMutation.isPending}>
              {createTaskMutation.isPending ? t('newTask.submitting') : t('newTask.submit')}
            </Button>
          </div>
        </form>
      </div>
    </main>
  );
}
