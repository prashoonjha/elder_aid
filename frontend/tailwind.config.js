/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      // Single source of truth for the visual theme. Components reference
      // these names (bg-brand-primary, font-display, etc.) instead of raw
      // hex values, so a future redesign means editing this file only.
      colors: {
        brand: {
          primary: '#16263F',
          primaryHover: '#0F1B2E',
          accent: '#0F7A6E',
          accentLight: '#E1F5EE',
          accentDark: '#085041',
          surface: '#F2F5F8',
          border: '#E3E7ED',
          textSecondary: '#6B7686',
          textMuted: '#9CA6B3',
        },
      },
      fontFamily: {
        display: ['"Space Grotesk"', 'sans-serif'],
        sans: ['Inter', 'sans-serif'],
      },
      borderRadius: {
        card: '14px',
        field: '8px',
        control: '10px',
      },
    },
  },
  plugins: [],
};

