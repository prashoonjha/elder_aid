import type { ButtonHTMLAttributes } from 'react';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary';
  fullWidth?: boolean;
}

export function Button({ variant = 'primary', fullWidth = true, className = '', ...props }: ButtonProps) {
  // min-h-12 keeps the 48px touch target the elderly-facing screens need.
  const base =
    'min-h-12 rounded-control px-5 py-3 text-base font-semibold transition-all ' +
    'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-accent focus-visible:ring-offset-2 ' +
    'disabled:opacity-50 disabled:pointer-events-none';
  const widthClass = fullWidth ? 'w-full' : '';
  const variantClass =
    variant === 'primary'
      ? 'bg-brand-primary text-white shadow-primary hover:bg-brand-primaryHover hover:-translate-y-px'
      : 'border border-brand-border bg-white text-brand-primary hover:bg-brand-surface';

  return <button className={`${base} ${widthClass} ${variantClass} ${className}`} {...props} />;
}
