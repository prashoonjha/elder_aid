import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { ArrowLeft, User, Users } from 'lucide-react';
import { ChoiceCard } from '../components/ui/ChoiceCard';

export function ForWhomPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();

  return (
    <main className="flex min-h-screen flex-col bg-white px-6 py-10">
      <button onClick={() => navigate(-1)} aria-label={t('common.back')} className="mb-4 text-brand-textMuted">
        <ArrowLeft size={20} />
      </button>

      <div className="mx-auto w-full max-w-sm">
        <h1 className="font-display text-xl font-bold text-brand-primary">{t('forWhom.title')}</h1>
        <p className="mb-6 mt-1 text-sm text-brand-textSecondary">{t('forWhom.subtitle')}</p>

        <ChoiceCard
          icon={<User size={22} />}
          title={t('forWhom.myself.title')}
          subtitle={t('forWhom.myself.subtitle')}
          onClick={() => navigate('/register', { state: { role: 'CLIENT', forSelf: true } })}
        />

        <ChoiceCard
          icon={<Users size={22} />}
          title={t('forWhom.familyMember.title')}
          subtitle={t('forWhom.familyMember.subtitle')}
          onClick={() => navigate('/register', { state: { role: 'FAMILY_MEMBER', forSelf: false } })}
        />
      </div>
    </main>
  );
}
