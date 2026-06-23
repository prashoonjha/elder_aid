import type { InputHTMLAttributes } from 'react';

interface TextFieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
}

export function TextField({ label, error, id, ...inputProps }: TextFieldProps) {
  const fieldId = id ?? label.toLowerCase().replace(/\s+/g, '-');

  return (
    <div className="mb-3">
      <label htmlFor={fieldId} className="mb-1 block text-xs text-brand-textSecondary">
        {label}
      </label>
      <input
        id={fieldId}
        className="w-full rounded-field bg-brand-surface px-3 py-2.5 text-sm text-slate-900 outline-none ring-brand-accent placeholder:text-brand-textMuted focus:ring-2"
        {...inputProps}
      />
      {error && <p className="mt-1 text-xs text-red-600">{error}</p>}
    </div>
  );
}
