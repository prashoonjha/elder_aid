import { useState, type FormEvent } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { ArrowLeft } from 'lucide-react';
import { isAxiosError } from 'axios';
import { TextField } from '../components/ui/TextField';
import { CheckboxField } from '../components/ui/CheckboxField';
import { Button } from '../components/ui/Button';
import { useAuth } from '../context/AuthContext';
import type { UserRole } from '../api/auth';

interface RegisterRouteState {
  role: UserRole;
  forSelf?: boolean;
}

export function RegisterPage() {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  const location = useLocation();
  const { register } = useAuth();

  const routeState = location.state as RegisterRouteState | null;

  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [termsAccepted, setTermsAccepted] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Someone landing here without going through the choice screens (e.g. a
  // bookmarked URL) doesn't have a role to register with - send them back
  // to start rather than letting the form submit with no role at all.
  if (!routeState?.role) {
    return <Navigate to="/" replace />;
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setErrorMessage(null);
    setIsSubmitting(true);

    try {
      await register({
        email,
        firstName,
        lastName,
        password,
        role: routeState.role,
        termsAccepted,
        locale: i18n.language === 'en' ? 'en' : 'fi',
      });
      // forSelf (whether this CLIENT is managing their own profile) gets
      // used by the elderly-profile onboarding step, which doesn't exist
      // yet - the dashboard is a deliberate placeholder until that's built.
      navigate('/dashboard', { replace: true });
    } catch (error) {
      if (isAxiosError(error) && error.response?.status === 409) {
        setErrorMessage(t('register.errors.emailInUse'));
      } else if (isAxiosError(error) && error.response?.status === 400) {
        setErrorMessage(t('register.errors.validation'));
      } else {
        setErrorMessage(t('register.errors.generic'));
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="flex min-h-screen flex-col bg-white px-6 py-10">
      <button onClick={() => navigate(-1)} aria-label={t('common.back')} className="mb-4 text-brand-textMuted">
        <ArrowLeft size={20} />
      </button>

      <div className="mx-auto w-full max-w-sm">
        <h1 className="font-display mb-6 text-xl font-bold text-brand-primary">{t('register.title')}</h1>

        <form onSubmit={handleSubmit}>
          <div className="flex gap-2">
            <div className="flex-1">
              <TextField
                label={t('register.firstName')}
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
                required
              />
            </div>
            <div className="flex-1">
              <TextField
                label={t('register.lastName')}
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
                required
              />
            </div>
          </div>

          <TextField
            label={t('register.email')}
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />

          <TextField
            label={t('register.password')}
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            minLength={10}
            required
          />

          <CheckboxField
            label={t('register.termsLabel')}
            checked={termsAccepted}
            onChange={(e) => setTermsAccepted(e.target.checked)}
            required
          />

          {errorMessage && <p className="mb-4 text-sm text-red-600">{errorMessage}</p>}

          <Button type="submit" disabled={isSubmitting}>
            {isSubmitting ? t('register.submitting') : t('register.submit')}
          </Button>
        </form>
      </div>
    </main>
  );
}
