import { useTranslation } from 'react-i18next';
import { Button } from '../components/ui/Button';
import { useAuth } from '../context/AuthContext';

export function DashboardPage() {
  const { t } = useTranslation();
  const { user, logout } = useAuth();

  return (
    <main className="flex min-h-screen flex-col items-center justify-center bg-white px-6 text-center">
      <p className="font-display text-xl font-bold text-brand-primary">{t('dashboard.welcomeBack')}</p>
      <p className="mt-2 text-sm text-brand-textSecondary">
        {user?.email} - {user?.roles.join(', ')}
      </p>
      <p className="mt-6 max-w-xs text-xs text-brand-textMuted">{t('dashboard.placeholderNote')}</p>

      <div className="mt-8 w-48">
        <Button variant="secondary" onClick={logout}>
          {t('dashboard.logOut')}
        </Button>
      </div>
    </main>
  );
}
