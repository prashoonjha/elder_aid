import type { ReactNode } from 'react';

interface ChoiceCardProps {
  icon: ReactNode;
  title: string;
  subtitle: string;
  selected?: boolean;
  onClick: () => void;
}

export function ChoiceCard({ icon, title, subtitle, selected = false, onClick }: ChoiceCardProps) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`mb-3 w-full rounded-card border-2 p-4 text-left transition-colors ${
        selected ? 'border-brand-accent bg-brand-accentLight' : 'border-brand-border bg-white hover:bg-brand-surface'
      }`}
    >
      <div className={selected ? 'text-brand-accentDark' : 'text-brand-primary'}>{icon}</div>
      <p className={`mt-2 text-sm font-medium ${selected ? 'text-brand-accentDark' : 'text-brand-primary'}`}>
        {title}
      </p>
      <p className={`mt-0.5 text-xs ${selected ? 'text-brand-accent' : 'text-brand-textSecondary'}`}>{subtitle}</p>
    </button>
  );
}
