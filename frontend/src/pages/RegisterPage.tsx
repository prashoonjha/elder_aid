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
  const [confirmPassword, setConfirmPassword] = useState('');
  const [phone, setPhone] = useState('');
  const [termsAccepted, setTermsAccepted] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Someone landing here without going through the choice screens (e.g. a
  // bookmarked URL) doesn't have a role to register with - send them back
  // to start rather than letting the form submit with no role at all.
  if (!routeState?.role) {
    return <Navigate to="/" replace />;
  }
  // Past the guard above routeState.role is guaranteed, but that narrowing
  // doesn't reach the submit handler, so capture it here.
  const registrationRole = routeState.role;

  // User types the part after +358. Drop spaces and a leading 0 (people type
  // "040..." out of habit, but +358 replaces that 0). Finnish mobiles start
  // with 4 or 5 and are 9 digits nationally.
  const phoneDigits = phone.replace(/\s/g, '').replace(/^0/, '');
  const phoneValid = phoneDigits === '' || /^[45]\d{8}$/.test(phoneDigits);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setErrorMessage(null);

    if (password !== confirmPassword) {
      setErrorMessage(t('register.errors.passwordMismatch'));
      return;
    }

    if (!phoneValid) {
      setErrorMessage(t('register.errors.phoneInvalid'));
      return;
    }

    setIsSubmitting(true);

    try {
      await register({
        email,
        firstName,
        lastName,
        password,
        phone: phoneDigits ? `+358${phoneDigits}` : undefined,
        role: registrationRole,
        termsAccepted,
        locale: i18n.language === 'en' ? 'en' : 'fi',
      });
      // Workers have nothing to set up before browsing tasks; clients and
      // family members need an elderly profile to exist before anything
      // else in the app makes sense, so send them there first.
      const destination = registrationRole === 'WORKER' ? '/dashboard' : '/profiles/new';
      navigate(destination, { replace: true });
    } catch (error) {
      if (isAxiosError<{ errorCode?: string }>(error) && error.response?.data?.errorCode === 'EMAIL_ALREADY_IN_USE') {
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
          {password.length > 0 && (
            <p className={`-mt-2 mb-3 text-xs ${password.length >= 10 ? 'text-brand-accent' : 'text-brand-textMuted'}`}>
              {password.length >= 10 ? t('register.passwordStrength.good') : t('register.passwordStrength.tooShort')}
            </p>
          )}

          <TextField
            label={t('register.confirmPassword')}
            type="password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            required
          />
          {confirmPassword.length > 0 && confirmPassword !== password && (
            <p className="-mt-2 mb-3 text-xs text-red-600">{t('register.errors.passwordMismatch')}</p>
          )}

          <div className="mb-4">
            <label htmlFor="phone" className="mb-1.5 block text-sm font-medium text-brand-primary">
              {t('register.phone')}
            </label>
            <div className="flex">
              <span className="flex min-h-12 items-center rounded-l-field border border-r-0 border-brand-border bg-brand-surface px-3 text-base text-brand-textSecondary">
                +358
              </span>
              <input
                id="phone"
                type="tel"
                inputMode="numeric"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                placeholder={t('register.phonePlaceholder')}
                aria-invalid={!phoneValid || undefined}
                className={`min-h-12 w-full rounded-r-field border bg-white px-3.5 py-3 text-base text-brand-primary placeholder:text-brand-textMuted focus:outline-none focus:ring-4 focus:ring-brand-accent/15
                  ${!phoneValid ? 'border-red-500 focus:border-red-500' : 'border-brand-border focus:border-brand-accent'}`}
              />
            </div>
            {!phoneValid ? (
              <p className="mt-1.5 text-sm text-red-600">{t('register.errors.phoneInvalid')}</p>
            ) : (
              <p className="mt-1.5 text-sm text-brand-textMuted">{t('register.phoneHint')}</p>
            )}
          </div>

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
