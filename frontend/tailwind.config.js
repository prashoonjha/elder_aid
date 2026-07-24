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
          // Cooler than the old #F2F5F8 so white cards read as raised.
          surface: '#F7F9FB',
          border: '#E3E7ED',
          borderSubtle: '#F0F3F6',
          textSecondary: '#6B7686',
          // Darkened from #9CA6B3 to clear WCAG AA on surface.
          textMuted: '#8A94A6',
        },
      },
      fontFamily: {
        display: ['"Space Grotesk"', 'sans-serif'],
        sans: ['Inter', 'sans-serif'],
      },
      borderRadius: {
        card: '14px',
        field: '9px',
        control: '10px',
      },
      boxShadow: {
        // Two-layer: a tight shadow for definition, a wide one for lift.
        card: '0 1px 2px rgba(22, 38, 63, 0.04), 0 4px 12px rgba(22, 38, 63, 0.07)',
        cardHover: '0 1px 2px rgba(22, 38, 63, 0.05), 0 8px 20px rgba(22, 38, 63, 0.10)',
        panel: '0 1px 2px rgba(22, 38, 63, 0.04), 0 8px 26px rgba(22, 38, 63, 0.08)',
        primary: '0 4px 12px rgba(22, 38, 63, 0.18)',
      },
      backgroundImage: {
        'brand-gradient': 'linear-gradient(160deg, #16263F 0%, #0E5F57 135%)',
      },
    },
  },
  plugins: [],
};
