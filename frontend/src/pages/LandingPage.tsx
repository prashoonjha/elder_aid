import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Menu, X, ShieldCheck, Lock, Languages, Star } from 'lucide-react';
import { LanguageToggle } from '../components/ui/LanguageToggle';

export function LandingPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);

  // Mirrors the old welcome screen: families pick who they're posting for,
  // workers skip straight to registration with the role already set.
  const goToClientSignup = () => navigate('/register/for-whom');
  const goToWorkerSignup = () => navigate('/register', { state: { role: 'WORKER' } });

  const steps = [
    { key: 'post', number: '1' },
    { key: 'choose', number: '2' },
    { key: 'follow', number: '3' },
  ];

  const features = [
    { key: 'verified', Icon: ShieldCheck },
    { key: 'privacy', Icon: Lock },
    { key: 'bilingual', Icon: Languages },
    { key: 'ratings', Icon: Star },
  ];

  return (
    <div className="min-h-screen bg-white">

      <header className="border-b border-brand-borderSubtle">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-5 py-4">
          <span className="font-display text-xl font-bold text-brand-primary">ElderAid</span>

          <nav className="hidden items-center gap-6 md:flex">
            <a href="#how-it-works" className="text-sm text-brand-textSecondary hover:text-brand-primary">
              {t('landing.nav.howItWorks')}
            </a>
            <a href="#about" className="text-sm text-brand-textSecondary hover:text-brand-primary">
              {t('landing.nav.about')}
            </a>
            <a href="#contact" className="text-sm text-brand-textSecondary hover:text-brand-primary">
              {t('landing.nav.contact')}
            </a>
            <LanguageToggle />
            <button
              onClick={() => navigate('/login')}
              className="text-sm font-medium text-brand-primary hover:text-brand-accent"
            >
              {t('landing.nav.logIn')}
            </button>
            <button
              onClick={goToClientSignup}
              className="rounded-control bg-brand-primary px-5 py-2.5 text-sm font-semibold text-white shadow-primary hover:bg-brand-primaryHover"
            >
              {t('landing.nav.getStarted')}
            </button>
          </nav>

          <button
            onClick={() => setMenuOpen((open) => !open)}
            aria-label={menuOpen ? t('landing.nav.closeMenu') : t('landing.nav.openMenu')}
            aria-expanded={menuOpen}
            className="text-brand-primary md:hidden"
          >
            {menuOpen ? <X size={24} /> : <Menu size={24} />}
          </button>
        </div>

        {menuOpen && (
          <nav className="border-t border-brand-borderSubtle px-5 py-4 md:hidden">
            <a href="#how-it-works" onClick={() => setMenuOpen(false)} className="block py-2.5 text-base text-brand-textSecondary">
              {t('landing.nav.howItWorks')}
            </a>
            <a href="#about" onClick={() => setMenuOpen(false)} className="block py-2.5 text-base text-brand-textSecondary">
              {t('landing.nav.about')}
            </a>
            <a href="#contact" onClick={() => setMenuOpen(false)} className="block py-2.5 text-base text-brand-textSecondary">
              {t('landing.nav.contact')}
            </a>
            <div className="py-2.5"><LanguageToggle /></div>
            <button onClick={() => navigate('/login')} className="block w-full py-2.5 text-left text-base font-medium text-brand-primary">
              {t('landing.nav.logIn')}
            </button>
          </nav>
        )}
      </header>

      <section className="relative overflow-hidden bg-brand-gradient px-5 py-14 md:py-20">
        <div className="pointer-events-none absolute -right-24 -top-32 h-80 w-80 rounded-full bg-white/[0.04]" />
        <div className="pointer-events-none absolute -left-16 bottom-0 h-48 w-48 rounded-full bg-white/[0.03]" />
        <div className="relative mx-auto max-w-6xl">
          <div className="max-w-2xl">
            <span className="mb-4 inline-block rounded-full bg-white/[0.13] px-3 py-1.5 text-sm text-white">
              {t('landing.hero.badge')}
            </span>
            <h1 className="font-display mb-4 text-4xl font-bold leading-tight text-white md:text-5xl">
              {t('landing.hero.title')}
            </h1>
            <p className="mb-8 text-lg leading-relaxed text-white/80">
              {t('landing.hero.subtitle')}
            </p>
            <div className="flex flex-col gap-3 sm:flex-row">
              <button
                onClick={goToClientSignup}
                className="min-h-12 rounded-control bg-white px-7 py-3.5 text-base font-semibold text-brand-primary transition-transform hover:-translate-y-0.5"
              >
                {t('landing.hero.needHelp')}
              </button>
              <button
                onClick={goToWorkerSignup}
                className="min-h-12 rounded-control border border-white/30 bg-white/[0.12] px-7 py-3.5 text-base font-semibold text-white transition-transform hover:-translate-y-0.5"
              >
                {t('landing.hero.wantToHelp')}
              </button>
            </div>
          </div>
        </div>
      </section>

      <section id="how-it-works" className="bg-brand-surface px-5 py-14">
        <div className="mx-auto max-w-6xl">
          <h2 className="font-display mb-2 text-center text-3xl font-bold text-brand-primary">
            {t('landing.howItWorks.title')}
          </h2>
          <p className="mb-10 text-center text-base text-brand-textSecondary">
            {t('landing.howItWorks.subtitle')}
          </p>
          <div className="grid gap-4 md:grid-cols-3">
            {steps.map(({ key, number }) => (
              <div key={key} className="rounded-card bg-white p-6 shadow-card">
                <div className="font-display mb-4 flex h-10 w-10 items-center justify-center rounded-control bg-brand-accentLight text-lg font-bold text-brand-accentDark">
                  {number}
                </div>
                <h3 className="mb-2 text-lg font-semibold text-brand-primary">
                  {t(`landing.howItWorks.${key}.title`)}
                </h3>
                <p className="text-base leading-relaxed text-brand-textSecondary">
                  {t(`landing.howItWorks.${key}.description`)}
                </p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section id="about" className="px-5 py-14">
        <div className="mx-auto grid max-w-6xl items-center gap-10 md:grid-cols-[1.2fr_1fr]">
          <div>
            <h2 className="font-display mb-4 text-3xl font-bold text-brand-primary">
              {t('landing.about.title')}
            </h2>
            <p className="mb-4 text-base leading-relaxed text-brand-textSecondary">
              {t('landing.about.paragraph1')}
            </p>
            <p className="text-base leading-relaxed text-brand-textSecondary">
              {t('landing.about.paragraph2')}
            </p>
          </div>
          <div className="grid gap-3 sm:grid-cols-2">
            {features.map(({ key, Icon }) => (
              <div key={key} className="rounded-card bg-brand-surface p-4">
                <Icon size={22} className="mb-2 text-brand-accent" />
                <p className="mb-1 text-base font-semibold text-brand-primary">
                  {t(`landing.about.features.${key}.title`)}
                </p>
                <p className="text-sm leading-relaxed text-brand-textSecondary">
                  {t(`landing.about.features.${key}.description`)}
                </p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section id="contact" className="bg-brand-surface px-5 py-14">
        <div className="mx-auto max-w-6xl text-center">
          <h2 className="font-display mb-2 text-3xl font-bold text-brand-primary">
            {t('landing.contact.title')}
          </h2>
          <p className="mb-8 text-base text-brand-textSecondary">
            {t('landing.contact.subtitle')}
          </p>
          <div className="grid gap-3 sm:grid-cols-3">
            <div className="rounded-card bg-white p-5 shadow-card">
              <p className="mb-1 text-sm text-brand-textMuted">{t('landing.contact.email')}</p>
              <p className="text-base font-semibold text-brand-primary">xxxxx@xxxxx.fi</p>
            </div>
            <div className="rounded-card bg-white p-5 shadow-card">
              <p className="mb-1 text-sm text-brand-textMuted">{t('landing.contact.phone')}</p>
              <p className="text-base font-semibold text-brand-primary">+358 xx xxx xxxx</p>
            </div>
            <div className="rounded-card bg-white p-5 shadow-card">
              <p className="mb-1 text-sm text-brand-textMuted">{t('landing.contact.address')}</p>
              <p className="text-base font-semibold text-brand-primary">xxxxx, Helsinki</p>
            </div>
          </div>
        </div>
      </section>

      <footer className="bg-brand-primary px-5 py-10">
        <div className="mx-auto max-w-6xl">
          <div className="mb-8 grid gap-8 sm:grid-cols-2 md:grid-cols-4">
            <div>
              <p className="font-display mb-2 text-lg font-bold text-white">ElderAid</p>
              <p className="text-sm leading-relaxed text-white/55">{t('landing.footer.tagline')}</p>
            </div>
            <div>
              <p className="mb-3 text-sm font-semibold text-white">{t('landing.footer.service')}</p>
              <a href="#how-it-works" className="mb-2 block text-sm text-white/55 hover:text-white">{t('landing.nav.howItWorks')}</a>
              <button onClick={goToWorkerSignup} className="mb-2 block text-sm text-white/55 hover:text-white">{t('landing.footer.forHelpers')}</button>
              <button onClick={() => navigate('/login')} className="block text-sm text-white/55 hover:text-white">{t('landing.nav.logIn')}</button>
            </div>
            <div>
              <p className="mb-3 text-sm font-semibold text-white">{t('landing.nav.contact')}</p>
              <p className="mb-2 text-sm text-white/55">xxxxx@xxxxx.fi</p>
              <p className="mb-2 text-sm text-white/55">+358 xx xxx xxxx</p>
              <p className="text-sm text-white/55">xxxxx, Helsinki</p>
            </div>
            <div>
              <p className="mb-3 text-sm font-semibold text-white">{t('landing.footer.legal')}</p>
              <Link to="/privacy-policy" className="mb-2 block text-sm text-white/55 hover:text-white">{t('landing.footer.privacyPolicy')}</Link>
              <Link to="/terms" className="block text-sm text-white/55 hover:text-white">{t('landing.footer.terms')}</Link>
            </div>
          </div>
          <div className="flex flex-col gap-2 border-t border-white/10 pt-6 sm:flex-row sm:justify-between">
            <p className="text-sm text-white/45">{t('landing.footer.copyright', { year: new Date().getFullYear() })}</p>
            <p className="text-sm text-white/45">{t('landing.footer.madeIn')}</p>
          </div>
        </div>
      </footer>

    </div>
  );
}
