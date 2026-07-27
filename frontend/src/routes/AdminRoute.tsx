import { Navigate, Outlet } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext';

export function AdminRoute() {
  const { isAuthenticated, isLoading, user } = useAuth();
  const { t } = useTranslation();

  if (isLoading) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-brand-surface">
        <p className="text-sm text-brand-textSecondary">{t('common.loading')}</p>
      </main>
    );
  }

  // Non-admins (including logged-out users) never see admin screens; send them
  // to the dashboard rather than login so an authenticated non-admin isn't
  // bounced out of their session.
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  if (!user?.roles.includes('ADMIN')) {
    return <Navigate to="/dashboard" replace />;
  }

  return <Outlet />;
}
