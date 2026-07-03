import { Navigate, Outlet } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext';

export function ProtectedRoute() {
  const { isAuthenticated, isLoading } = useAuth();
  const { t } = useTranslation();

  // While the app is still checking whether the refresh cookie can restore a
  // session, don't redirect yet - otherwise a page reload on a protected
  // route would bounce to login before the silent refresh even finishes,
  // which is exactly what this whole feature is meant to prevent.
  if (isLoading) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-brand-surface">
        <p className="text-sm text-brand-textSecondary">{t('common.loading')}</p>
      </main>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
}
