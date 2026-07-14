import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { Plus, UserPlus, ClipboardList, Search, CalendarCheck, ShieldCheck } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

interface ActionCardProps {
  icon: React.ReactNode;
  title: string;
  description: string;
  onClick: () => void;
}

function ActionCard({ icon, title, description, onClick }: ActionCardProps) {
  return (
    <button
      onClick={onClick}
      className="rounded-card border border-brand-border bg-white p-4 text-left transition-shadow hover:shadow-md"
    >
      <div className="mb-2.5 flex h-8 w-8 items-center justify-center rounded-control bg-brand-accentLight text-brand-accent">
        {icon}
      </div>
      <p className="mb-1 text-sm font-semibold text-brand-primary">{title}</p>
      <p className="text-xs leading-relaxed text-brand-textSecondary">{description}</p>
    </button>
  );
}

export function DashboardPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const canManageProfiles = user?.roles.some((role) => role === 'CLIENT' || role === 'FAMILY_MEMBER') ?? false;
  const isWorker = user?.roles.includes('WORKER') ?? false;

  // Workers are the only role with a verification tier; NONE means they've
  // submitted nothing yet, so there's nothing worth badging.
  const isVerified = isWorker && user?.verificationTier != null && user.verificationTier !== 'NONE';

  const roleLabel = isWorker ? t('dashboard.roleWorker') : t('dashboard.roleFamily');

  return (
    <main className="min-h-screen bg-brand-surface px-6 py-8">
      <div className="mx-auto max-w-5xl">

        <header className="mb-6 flex flex-wrap items-center justify-between gap-3 border-b border-brand-border pb-4">
          <div>
            <h1 className="font-display text-xl font-bold text-brand-primary">
              {t('dashboard.welcomeBackNamed', { name: user?.firstName })}
            </h1>
            <p className="mt-0.5 flex items-center gap-1.5 text-xs text-brand-textSecondary">
              {roleLabel}
              {isVerified && (
                <span className="flex items-center gap-1 text-brand-accent">
                  <ShieldCheck size={12} />
                  {t('dashboard.verified')}
                </span>
              )}
            </p>
          </div>
          <div className="flex items-center gap-4">
            <button
              onClick={() => navigate('/settings/privacy')}
              className="text-xs font-medium text-brand-textMuted hover:text-brand-primary"
            >
              {t('dashboard.privacy')}
            </button>
            <button
              onClick={logout}
              className="rounded-control border border-brand-border bg-white px-4 py-2 text-xs font-medium text-brand-primary"
            >
              {t('dashboard.logOut')}
            </button>
          </div>
        </header>

        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
          {canManageProfiles && (
            <>
              <ActionCard
                icon={<Plus size={16} />}
                title={t('dashboard.cards.postTask.title')}
                description={t('dashboard.cards.postTask.description')}
                onClick={() => navigate('/tasks/new')}
              />
              <ActionCard
                icon={<UserPlus size={16} />}
                title={t('dashboard.cards.addProfile.title')}
                description={t('dashboard.cards.addProfile.description')}
                onClick={() => navigate('/profiles/new')}
              />
              <ActionCard
                icon={<ClipboardList size={16} />}
                title={t('dashboard.cards.myTasks.title')}
                description={t('dashboard.cards.myTasks.description')}
                onClick={() => navigate('/tasks/mine')}
              />
            </>
          )}

          {isWorker && (
            <>
              <ActionCard
                icon={<Search size={16} />}
                title={t('dashboard.cards.browseTasks.title')}
                description={t('dashboard.cards.browseTasks.description')}
                onClick={() => navigate('/tasks')}
              />
              <ActionCard
                icon={<CalendarCheck size={16} />}
                title={t('dashboard.cards.myBookings.title')}
                description={t('dashboard.cards.myBookings.description')}
                onClick={() => navigate('/bookings/mine')}
              />
            </>
          )}
        </div>

      </div>
    </main>
  );
}
