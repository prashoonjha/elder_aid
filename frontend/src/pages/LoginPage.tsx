import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { isAxiosError } from 'axios';
import { TextField } from '../components/ui/TextField';
import { Button } from '../components/ui/Button';
import { LanguageToggle } from '../components/ui/LanguageToggle';
import { useAuth } from '../context/AuthContext';

export function LoginPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { login } = useAuth();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setErrorMessage(null);
    setIsSubmitting(true);

    try {
      await login({ email, password });
      navigate('/dashboard', { replace: true });
    } catch (error) {
      if (isAxiosError(error) && error.response?.status === 401) {
        setErrorMessage(t('login.errors.invalidCredentials'));
      } else {
        setErrorMessage(t('login.errors.generic'));
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="flex min-h-screen flex-col bg-white px-6 py-10">
      <div className="mx-auto w-full max-w-sm flex-1">
        <p className="font-display text-xl font-bold text-brand-primary">{t('common.appName')}</p>
        <p className="mb-7 mt-1 text-sm text-brand-textSecondary">{t('common.tagline')}</p>

        <form onSubmit={handleSubmit}>
          <TextField
            label={t('login.email')}
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
          <TextField
            label={t('login.password')}
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />

          {errorMessage && <p className="mb-4 text-sm text-red-600">{errorMessage}</p>}

          <Button type="submit" disabled={isSubmitting}>
            {isSubmitting ? t('login.submitting') : t('login.submit')}
          </Button>
        </form>

        <p className="mt-6 text-center text-xs text-brand-textMuted">
          {t('login.noAccount')}{' '}
          <button onClick={() => navigate('/')} className="font-medium text-brand-accent">
            {t('login.createOne')}
          </button>
        </p>
      </div>

      <LanguageToggle />
    </main>
  );
}
