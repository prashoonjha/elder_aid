import type { InputHTMLAttributes } from 'react';

interface CheckboxFieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
}

export function CheckboxField({ label, error, id, ...inputProps }: CheckboxFieldProps) {
  const fieldId = id ?? 'checkbox';

  return (
    <div className="mb-4">
      <div className="flex items-start gap-2">
        <input
          id={fieldId}
          type="checkbox"
          className="mt-0.5 h-4 w-4 shrink-0 rounded border-brand-textMuted text-brand-accent focus:ring-brand-accent"
          {...inputProps}
        />
        <label htmlFor={fieldId} className="text-xs leading-relaxed text-brand-textSecondary">
          {label}
        </label>
      </div>
      {error && <p className="mt-1 text-xs text-red-600">{error}</p>}
    </div>
  );
}
