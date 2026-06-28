import { useTranslation } from 'react-i18next';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { listMyBookings, checkIn, checkOut, type BookingDetail } from '../api/bookings';
import { Button } from '../components/ui/Button';
import { TASK_CATEGORIES } from '../constants/taskCategories';

const STATUS_BADGE_STYLES: Record<string, string> = {
  CONFIRMED: 'bg-blue-50 text-blue-700',
  CHECKED_IN: 'bg-amber-50 text-amber-700',
  COMPLETED: 'bg-brand-accentLight text-brand-accentDark',
  CANCELLED: 'bg-brand-surface text-brand-textMuted',
  DISPUTED: 'bg-red-50 text-red-700',
};

export function MyBookingsPage() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();

  const bookingsQuery = useQuery({
    queryKey: ['bookings', 'mine'],
    queryFn: listMyBookings,
  });

  function invalidate() {
    queryClient.invalidateQueries({ queryKey: ['bookings', 'mine'] });
  }

  const checkInMutation = useMutation({ mutationFn: checkIn, onSuccess: invalidate });
  const checkOutMutation = useMutation({ mutationFn: checkOut, onSuccess: invalidate });

  function renderAction(booking: BookingDetail) {
    if (booking.status === 'CONFIRMED') {
      return (
        <Button onClick={() => checkInMutation.mutate(booking.id)} disabled={checkInMutation.isPending}>
          {t('myBookings.checkIn')}
        </Button>
      );
    }
    if (booking.status === 'CHECKED_IN') {
      return (
        <Button onClick={() => checkOutMutation.mutate(booking.id)} disabled={checkOutMutation.isPending}>
          {t('myBookings.checkOut')}
        </Button>
      );
    }
    return null;
  }

  return (
    <main className="min-h-screen bg-brand-surface px-4 py-6">
      <div className="mx-auto max-w-md">
        <h1 className="font-display mb-4 text-lg font-bold text-brand-primary">{t('myBookings.title')}</h1>

        {bookingsQuery.isLoading && <p className="text-sm text-brand-textSecondary">{t('common.loading')}</p>}

        {bookingsQuery.data && bookingsQuery.data.length === 0 && (
          <p className="mt-8 text-center text-sm text-brand-textSecondary">{t('myBookings.empty')}</p>
        )}

        <div className="flex flex-col gap-2.5">
          {bookingsQuery.data?.map((booking) => {
            const categoryConfig = TASK_CATEGORIES.find((c) => c.value === booking.taskCategory);
            const Icon = categoryConfig?.icon;
            return (
              <div key={booking.id} className="rounded-card border border-brand-border bg-white p-3">
                <div className="mb-1 flex items-center justify-between">
                  <div className="flex items-center gap-1.5">
                    {Icon && <Icon size={15} className="text-brand-accent" />}
                    <span className="text-xs font-semibold text-brand-primary">
                      {categoryConfig && t(categoryConfig.labelKey)}
                    </span>
                  </div>
                  <span className={`rounded-full px-2.5 py-1 text-[10px] font-medium ${STATUS_BADGE_STYLES[booking.status] ?? ''}`}>
                    {t(`myBookings.status.${booking.status}`)}
                  </span>
                </div>
                <p className="mb-3 text-xs text-brand-textMuted">
                  {new Date(booking.taskScheduledStart).toLocaleString()} · {booking.taskCity}
                </p>
                {renderAction(booking)}
              </div>
            );
          })}
        </div>
      </div>
    </main>
  );
}
