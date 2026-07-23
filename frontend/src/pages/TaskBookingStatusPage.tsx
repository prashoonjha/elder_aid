import { useState, type FormEvent } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft, CheckCircle, Clock, MapPin } from 'lucide-react';
import { getBookingByTask } from '../api/bookings';
import { submitReview } from '../api/reviews';
import { StarRating } from '../components/ui/StarRating';
import { TASK_CATEGORIES } from '../constants/taskCategories';

// Mirrors the backend's @Size(max = 2000) on CreateReviewRequest.comment.
const REVIEW_COMMENT_MAX = 2000;

const STATUS_STYLES: Record<string, string> = {
  CONFIRMED: 'bg-blue-50 text-blue-700',
  CHECKED_IN: 'bg-amber-50 text-amber-700',
  COMPLETED: 'bg-brand-accentLight text-brand-accentDark',
};

export function TaskBookingStatusPage() {
  const { taskId } = useParams<{ taskId: string }>();
  const { t } = useTranslation();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const [rating, setRating] = useState(0);
  const [comment, setComment] = useState('');

  const bookingQuery = useQuery({
    queryKey: ['booking', 'byTask', taskId],
    queryFn: () => getBookingByTask(taskId!),
    enabled: !!taskId,
    // Poll every 30 seconds so the family member sees check-in/check-out
    // happen without needing to manually refresh the page.
    refetchInterval: 30_000,
  });

  const reviewMutation = useMutation({
    mutationFn: () => submitReview(bookingQuery.data!.id, { rating, comment: comment.trim() || undefined }),
    onSuccess: () => {
      // Refetch the booking so it comes back with existingReview populated,
      // which flips the UI to the read-only state.
      queryClient.invalidateQueries({ queryKey: ['booking', 'byTask', taskId] });
    },
  });

  function handleReviewSubmit(event: FormEvent) {
    event.preventDefault();
    if (rating < 1) return;
    reviewMutation.mutate();
  }

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

        {booking.status === 'COMPLETED' && (
          <div className="mb-4 border-t border-brand-border pt-4">
            {booking.existingReview ? (
              <>
                <p className="mb-2 text-xs font-semibold text-brand-primary">{t('review.yourRating')}</p>
                <StarRating value={booking.existingReview.rating} size={22} readOnly />
                {booking.existingReview.comment && (
                  <p className="mt-2.5 text-xs italic text-brand-textSecondary">
                    "{booking.existingReview.comment}"
                  </p>
                )}
              </>
            ) : (
              <form onSubmit={handleReviewSubmit}>
                <p className="mb-2 text-xs font-semibold text-brand-primary">{t('review.rateWorker')}</p>
                <div className="mb-3">
                  <StarRating value={rating} onChange={setRating} />
                </div>
                <textarea
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  placeholder={t('review.commentPlaceholder')}
                  maxLength={REVIEW_COMMENT_MAX}
                  rows={2}
                  className="w-full rounded-field border border-brand-border bg-brand-surface px-3 py-2 text-sm text-slate-900 outline-none ring-brand-accent placeholder:text-brand-textMuted focus:ring-2"
                />
                {/* Only worth showing once they're typing - the comment is
                    optional, so "0 / 2000" on an empty box is just noise. */}
                <p
                  className={`mb-3 mt-1 text-right text-xs ${
                    comment.length >= REVIEW_COMMENT_MAX - 100 ? 'text-amber-600' : 'text-brand-textMuted'
                  } ${comment.length === 0 ? 'invisible' : ''}`}
                >
                  {comment.length} / {REVIEW_COMMENT_MAX}
                </p>
                {reviewMutation.isError && (
                  <p className="mb-3 text-xs text-red-600">{t('review.error')}</p>
                )}
                <button
                  type="submit"
                  disabled={rating < 1 || reviewMutation.isPending}
                  className="w-full rounded-control bg-brand-primary py-2.5 text-sm font-medium text-white disabled:opacity-50"
                >
                  {reviewMutation.isPending ? t('review.submitting') : t('review.submit')}
                </button>
              </form>
            )}
          </div>
        )}

        <p className="text-xs text-brand-textMuted">{t('bookingStatus.autoRefreshNote')}</p>
      </div>
    </main>
  );
}
