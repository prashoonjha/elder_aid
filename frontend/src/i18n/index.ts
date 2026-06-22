import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

import fi from './locales/fi.json';
import en from './locales/en.json';

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources: {
      fi: { translation: fi },
      en: { translation: en },
    },
    fallbackLng: 'fi',
    interpolation: {
      escapeValue: false, // React already escapes output, so this is unnecessary here
    },
    detection: {
      // Remember the choice across visits rather than re-detecting browser
      // language every time - a Finnish speaker who picks English once
      // shouldn't have to repick it on every page load.
      order: ['localStorage', 'navigator'],
      caches: ['localStorage'],
    },
  });

export default i18n;
