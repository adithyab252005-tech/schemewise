import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

const resources = {
  en: {
    translation: {
      "dashboard": "Dashboard",
      "explore": "Explore",
      "ai_assistant": "AI Assistant",
      "updates": "Updates",
      "saved": "Saved",
      "settings": "Settings",
      "welcome": "Welcome back",
      "eligibility_score": "Eligibility Score",
      "total_value": "Total Value",
      "search_placeholder": "Search schemes...",
      "apply_now": "Apply Now",
      "view_details": "View Details",
      "profile": "Profile",
      "logout": "Logout",
      // Add more standard strings here
    }
  },
  hi: {
    translation: {
      "dashboard": "डैशबोर्ड",
      "explore": "खोजें",
      "ai_assistant": "एआई सहायक",
      "updates": "अपडेट",
      "saved": "सहेजा गया",
      "settings": "सेटिंग्स",
      "welcome": "वापसी पर स्वागत है",
      "eligibility_score": "पात्रता स्कोर",
      "total_value": "कुल मूल्य",
      "search_placeholder": "योजनाएं खोजें...",
      "apply_now": "अभी आवेदन करें",
      "view_details": "विवरण देखें",
      "profile": "प्रोफ़ाइल",
      "logout": "लॉग आउट",
    }
  }
};

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources,
    fallbackLng: 'en',
    interpolation: {
      escapeValue: false, // react already safes from xss
    },
    detection: {
      order: ['localStorage', 'navigator'],
      caches: ['localStorage'],
    }
  });

export default i18n;
