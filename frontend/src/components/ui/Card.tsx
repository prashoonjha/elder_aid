import type { ReactNode } from 'react';

interface CardProps {
  children: ReactNode;
  className?: string;
  // Renders as a button when given, with hover lift.
  onClick?: () => void;
  accent?: boolean;
}

export function Card({ children, className = '', onClick, accent = false }: CardProps) {
  const base = `rounded-card bg-white p-4 shadow-card ${accent ? 'border-l-[3px] border-brand-accent' : ''}`;

  if (onClick) {
    return (
      <button
        onClick={onClick}
        className={`${base} w-full text-left transition-all hover:-translate-y-0.5 hover:shadow-cardHover
          focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-accent focus-visible:ring-offset-2 ${className}`}
      >
        {children}
      </button>
    );
  }

  return <div className={`${base} ${className}`}>{children}</div>;
}
