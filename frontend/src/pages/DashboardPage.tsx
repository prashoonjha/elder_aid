import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { Button } from '../components/ui/Button';
import { useAuth } from '../context/AuthContext';

export function DashboardPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const canManageProfiles = user?.roles.some((role) => role === 'CLIENT' || role === 'FAMILY_MEMBER') ?? false;
  const isWorker = user?.roles.includes('WORKER') ?? false;

  return (
    <main className="flex min-h-screen flex-col items-center justify-center bg-white px-6 text-center">
      <p className="font-display text-xl font-bold text-brand-primary">{t('dashboard.welcomeBack')}</p>
      <p className="mt-2 text-sm text-brand-textSecondary">
        {user?.email} - {user?.roles.join(', ')}
      </p>
      <p className="mt-6 max-w-xs text-xs text-brand-textMuted">{t('dashboard.placeholderNote')}</p>

      {canManageProfiles && (
        <>
          <button onClick={() => navigate('/profiles/new')} className="mt-5 text-sm font-medium text-brand-accent">
            {t('dashboard.addProfile')}
          </button>
          <button onClick={() => navigate('/tasks/new')} className="mt-2 text-sm font-medium text-brand-accent">
            {t('dashboard.postTask')}
          </button>
          <button onClick={() => navigate('/tasks/mine')} className="mt-2 text-sm font-medium text-brand-accent">
            {t('dashboard.myTasks')}
          </button>
        </>
      )}

      {isWorker && (
        <button onClick={() => navigate('/tasks')} className="mt-5 text-sm font-medium text-brand-accent">
          {t('dashboard.browseTasks')}
        </button>
      )}

      <div className="mt-8 w-48">
        <Button variant="secondary" onClick={logout}>
          {t('dashboard.logOut')}
        </Button>
      </div>
    </main>
  );
}
