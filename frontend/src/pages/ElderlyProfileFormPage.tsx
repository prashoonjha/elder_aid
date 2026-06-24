import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { isAxiosError } from 'axios';
import { TextField } from '../components/ui/TextField';
import { Button } from '../components/ui/Button';
import { SegmentedControl } from '../components/ui/SegmentedControl';
import { useAuth } from '../context/AuthContext';
import { createElderlyProfile } from '../api/elderlyProfiles';

export function ElderlyProfileFormPage() {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  const { user } = useAuth();

  // CLIENT accounts are more often the elderly person themselves, so
  // default the toggle accordingly - it's still freely editable, this is
  // just a sensible starting guess, not a hard rule tied to the role.
  const [forSelf, setForSelf] = useState(user?.roles.includes('CLIENT') ?? false);

  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [relationship, setRelationship] = useState('');
  const [addressLine, setAddressLine] = useState('');
  const [postalCode, setPostalCode] = useState('');
  const [city, setCity] = useState('');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setErrorMessage(null);
    setIsSubmitting(true);

    try {
      await createElderlyProfile({
        firstName,
        lastName,
        addressLine,
        postalCode,
        city,
        preferredLanguage: i18n.language === 'en' ? 'en' : 'fi',
        relationship: forSelf ? undefined : relationship,
        forSelf,
      });
      navigate('/dashboard', { replace: true });
    } catch (error) {
      if (isAxiosError(error) && error.response?.status === 400) {
        setErrorMessage(t('elderlyProfile.errors.validation'));
      } else {
        setErrorMessage(t('elderlyProfile.errors.generic'));
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-brand-surface px-6 py-10">
      <div className="w-full max-w-lg rounded-card border border-brand-border bg-white p-9">
        <h1 className="font-display text-lg font-bold text-brand-primary">{t('elderlyProfile.title')}</h1>
        <p className="mb-5 mt-1 text-sm text-brand-textSecondary">{t('elderlyProfile.subtitle')}</p>

        <SegmentedControl
          value={forSelf ? 'self' : 'family'}
          onChange={(value) => setForSelf(value === 'self')}
          options={[
            { value: 'self', label: t('elderlyProfile.myself') },
            { value: 'family', label: t('elderlyProfile.familyMember') },
          ]}
        />

        <hr className="mb-5 border-brand-border" />

        <form onSubmit={handleSubmit}>
          <div className="grid grid-cols-2 gap-3">
            <TextField label={t('elderlyProfile.firstName')} value={firstName} onChange={(e) => setFirstName(e.target.value)} required />
            <TextField label={t('elderlyProfile.lastName')} value={lastName} onChange={(e) => setLastName(e.target.value)} required />
          </div>

          {!forSelf && (
            <TextField
              label={t('elderlyProfile.relationship')}
              value={relationship}
              onChange={(e) => setRelationship(e.target.value)}
              placeholder={t('elderlyProfile.relationshipPlaceholder')}
              required
            />
          )}

          <div className="grid grid-cols-3 gap-3">
            <div className="col-span-2">
              <TextField label={t('elderlyProfile.address')} value={addressLine} onChange={(e) => setAddressLine(e.target.value)} />
            </div>
            <TextField label={t('elderlyProfile.postalCode')} value={postalCode} onChange={(e) => setPostalCode(e.target.value)} />
          </div>

          <TextField label={t('elderlyProfile.city')} value={city} onChange={(e) => setCity(e.target.value)} />

          {errorMessage && <p className="mb-4 text-sm text-red-600">{errorMessage}</p>}

          <div className="mt-2 flex justify-end gap-3">
            <Button type="button" variant="secondary" fullWidth={false} onClick={() => navigate('/dashboard')}>
              {t('elderlyProfile.skip')}
            </Button>
            <Button type="submit" fullWidth={false} disabled={isSubmitting}>
              {isSubmitting ? t('elderlyProfile.submitting') : t('elderlyProfile.submit')}
            </Button>
          </div>
        </form>
      </div>
    </main>
  );
}
