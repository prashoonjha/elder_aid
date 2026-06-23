import { useTranslation } from 'react-i18next';

export function LanguageToggle() {
  const { i18n } = useTranslation();

  const pillClass = (lang: string) =>
    i18n.language === lang
      ? 'rounded-full bg-brand-accentLight px-3 py-1 text-xs font-medium text-brand-accentDark'
      : 'rounded-full px-3 py-1 text-xs text-brand-textMuted';

  return (
    <div className="flex items-center justify-center gap-2">
      <button type="button" className={pillClass('fi')} onClick={() => i18n.changeLanguage('fi')}>
        Suomi
      </button>
      <button type="button" className={pillClass('en')} onClick={() => i18n.changeLanguage('en')}>
        English
      </button>
    </div>
  );
}
