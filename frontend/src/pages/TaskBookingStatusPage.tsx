import { useParams, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useQuery } from '@tanstack/react-query';
import { ArrowLeft, CheckCircle, Clock, MapPin } from 'lucide-react';
import { getBookingByTask } from '../api/bookings';
import { TASK_CATEGORIES } from '../constants/taskCategories';

const STATUS_STYLES: Record<string, string> = {
  CONFIRMED: 'bg-blue-50 text-blue-700',
  CHECKED_IN: 'bg-amber-50 text-amber-700',
  COMPLETED: 'bg-brand-accentLight text-brand-accentDark',
};

export function TaskBookingStatusPage() {
  const { taskId } = useParams<{ taskId: string }>();
  const { t } = useTranslation();
  const navigate = useNavigate();

  const bookingQuery = useQuery({
    queryKey: ['booking', 'byTask', taskId],
    queryFn: () => getBookingByTask(taskId!),
    enabled: !!taskId,
    // Poll every 30 seconds so the family member sees check-in/check-out
    // happen without needing to manually refresh the page.
    refetchInterval: 30_000,
  });

  if (bookingQuery.isLoading) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-brand-surface">
        <p className="text-sm text-brand-textSecondary">{t('common.loading')}</p>
      </main>
    );
  }

  const booking = bookingQuery.data;
  if (!booking) return null;

  const categoryConfig = TASK_CATEGORIES.find((c) => c.value === booking.taskCategory);
  const Icon = categoryConfig?.icon;

  return (
    <main className="flex min-h-screen items-center justify-center bg-brand-surface px-6 py-10">
      <div className="w-full max-w-md rounded-card border border-brand-border bg-white p-8">
        <button onClick={() => navigate('/tasks/mine')} aria-label={t('common.back')} className="mb-4 text-brand-textMuted">
          <ArrowLeft size={20} />
        </button>

        <div className="mb-1 flex items-center gap-1.5">
          {Icon && <Icon size={14} className="text-brand-accent" />}
          <span className="text-xs font-medium text-brand-accent">{categoryConfig && t(categoryConfig.labelKey)}</span>
        </div>

        <h1 className="font-display mb-4 text-lg font-bold text-brand-primary">{t('bookingStatus.title')}</h1>

        <div className="mb-4 flex items-center gap-2">
          <span className={`rounded-full px-3 py-1 text-xs font-medium ${STATUS_STYLES[booking.status] ?? ''}`}>
            {t(`bookingStatus.status.${booking.status}`)}
          </span>
        </div>

        <div className="mb-4 flex flex-col gap-2.5 rounded-control border border-brand-border bg-brand-surface p-4">
          <div className="flex items-center gap-2 text-xs text-brand-textSecondary">
            <Clock size={13} className="text-brand-accent" />
            <span>{new Date(booking.taskScheduledStart).toLocaleString()}</span>
          </div>
          {booking.taskCity && (
            <div className="flex items-center gap-2 text-xs text-brand-textSecondary">
              <MapPin size={13} className="text-brand-accent" />
              <span>{booking.taskCity}</span>
            </div>
          )}
          {booking.checkInTime && (
            <div className="flex items-center gap-2 text-xs text-brand-textSecondary">
              <CheckCircle size={13} className="text-brand-accent" />
              <span>{t('bookingStatus.checkedInAt')} {new Date(booking.checkInTime).toLocaleTimeString()}</span>
            </div>
          )}
          {booking.checkOutTime && (
            <div className="flex items-center gap-2 text-xs text-brand-textSecondary">
              <CheckCircle size={13} className="text-brand-accentDark" />
              <span>{t('bookingStatus.completedAt')} {new Date(booking.checkOutTime).toLocaleTimeString()}</span>
            </div>
          )}
        </div>

        <p className="text-xs text-brand-textMuted">{t('bookingStatus.autoRefreshNote')}</p>
      </div>
    </main>
  );
}
