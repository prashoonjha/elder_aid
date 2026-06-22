import { useTranslation } from 'react-i18next';

export function HomePage() {
  const { t, i18n } = useTranslation();

  return (
    <main className="flex min-h-screen flex-col items-center justify-center gap-4 bg-slate-50 px-6 text-center">
      <h1 className="text-4xl font-bold text-slate-900">{t('common.appName')}</h1>
      <p className="text-lg text-slate-600">{t('common.tagline')}</p>

      <div className="mt-8 rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
        <h2 className="text-xl font-semibold text-slate-800">{t('home.greeting')}</h2>
        <p className="mt-2 max-w-md text-slate-600">{t('home.description')}</p>
      </div>

      <div className="mt-6 flex gap-2">
        <button
          onClick={() => i18n.changeLanguage('fi')}
          className="rounded-md border border-slate-300 px-3 py-1.5 text-sm font-medium text-slate-700 hover:bg-slate-100"
        >
          Suomi
        </button>
        <button
          onClick={() => i18n.changeLanguage('en')}
          className="rounded-md border border-slate-300 px-3 py-1.5 text-sm font-medium text-slate-700 hover:bg-slate-100"
        >
          English
        </button>
      </div>
    </main>
  );
}
