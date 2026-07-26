import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { ArrowLeft, FileText } from 'lucide-react';

interface LegalPlaceholderPageProps {
  titleKey: string;
}

export function LegalPlaceholderPage({ titleKey }: LegalPlaceholderPageProps) {
  const { t } = useTranslation();
  const navigate = useNavigate();

  return (
    <main className="flex min-h-screen items-center justify-center bg-brand-surface px-5 py-12">
      <div className="w-full max-w-lg rounded-card bg-white p-8 text-center shadow-panel">
        <div className="mx-auto mb-5 flex h-14 w-14 items-center justify-center rounded-card bg-brand-accentLight">
          <FileText size={26} className="text-brand-accent" />
        </div>
        <h1 className="font-display mb-3 text-2xl font-bold text-brand-primary">{t(titleKey)}</h1>
        <p className="mb-7 text-base leading-relaxed text-brand-textSecondary">
          {t('legal.comingSoon')}
        </p>
        <button
          onClick={() => navigate('/')}
          className="inline-flex min-h-12 items-center gap-2 rounded-control border border-brand-border px-5 py-3 text-base font-semibold text-brand-primary hover:bg-brand-surface"
        >
          <ArrowLeft size={18} />
          {t('legal.backHome')}
        </button>
      </div>
    </main>
  );
}
