import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Heart, Briefcase } from 'lucide-react';
import { ChoiceCard } from '../components/ui/ChoiceCard';
import { LanguageToggle } from '../components/ui/LanguageToggle';

export function WelcomePage() {
  const { t } = useTranslation();
  const navigate = useNavigate();

  return (
    <main className="flex min-h-screen flex-col bg-white px-6 py-10">
      <div className="mx-auto w-full max-w-sm flex-1">
        <h1 className="font-display text-xl font-bold text-brand-primary">{t('welcome.title')}</h1>
        <p className="mb-6 mt-1 text-sm text-brand-textSecondary">{t('welcome.subtitle')}</p>

        <ChoiceCard
          icon={<Heart size={22} />}
          title={t('welcome.needHelp.title')}
          subtitle={t('welcome.needHelp.subtitle')}
          onClick={() => navigate('/register/for-whom')}
        />

        <ChoiceCard
          icon={<Briefcase size={22} />}
          title={t('welcome.wantToHelp.title')}
          subtitle={t('welcome.wantToHelp.subtitle')}
          onClick={() => navigate('/register', { state: { role: 'WORKER' } })}
        />
      </div>

      <div className="mt-8">
        <LanguageToggle />
        <p className="mt-4 text-center text-xs text-brand-textMuted">
          {t('welcome.haveAccount')}{' '}
          <button onClick={() => navigate('/login')} className="font-medium text-brand-accent">
            {t('welcome.logIn')}
          </button>
        </p>
      </div>
    </main>
  );
}
