import type { ButtonHTMLAttributes } from 'react';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary';
  fullWidth?: boolean;
}

export function Button({ variant = 'primary', fullWidth = true, className = '', ...props }: ButtonProps) {
  const base = 'rounded-control py-2.5 text-sm font-medium transition-colors disabled:opacity-50';
  const widthClass = fullWidth ? 'w-full' : '';
  const variantClass =
    variant === 'primary'
      ? 'bg-brand-primary text-white hover:bg-brand-primaryHover'
      : 'border border-brand-border bg-white text-brand-primary hover:bg-brand-surface';

  return <button className={`${base} ${widthClass} ${variantClass} ${className}`} {...props} />;
}
