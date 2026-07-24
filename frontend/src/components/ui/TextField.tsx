import { useState, type InputHTMLAttributes } from 'react';
import { Eye, EyeOff } from 'lucide-react';
import { useTranslation } from 'react-i18next';

interface TextFieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
  hint?: string;
}

export function TextField({ label, error, hint, id, type, ...inputProps }: TextFieldProps) {
  const { t } = useTranslation();
  const fieldId = id ?? label.toLowerCase().replace(/\s+/g, '-');

  const isPassword = type === 'password';
  const [revealed, setRevealed] = useState(false);
  // Swap password -> text when revealed; leave any other type untouched.
  const effectiveType = isPassword && revealed ? 'text' : type;

  return (
    <div className="mb-4">
      <label htmlFor={fieldId} className="mb-1.5 block text-sm font-medium text-brand-primary">
        {label}
      </label>
      <div className="relative">
        <input
          id={fieldId}
          type={effectiveType}
          aria-invalid={error ? true : undefined}
          className={`min-h-12 w-full rounded-field border bg-white px-3.5 py-3 text-base text-brand-primary transition-shadow
            placeholder:text-brand-textMuted focus:outline-none focus:ring-4 focus:ring-brand-accent/15
            ${error ? 'border-red-500 focus:border-red-500' : 'border-brand-border focus:border-brand-accent'}
            ${isPassword ? 'pr-12' : ''}`}
          {...inputProps}
        />
        {isPassword && (
          <button
            type="button"
            onClick={() => setRevealed((r) => !r)}
            aria-label={revealed ? t('common.hidePassword') : t('common.showPassword')}
            className="absolute inset-y-0 right-0 flex items-center px-3.5 text-brand-textMuted hover:text-brand-primary"
          >
            {revealed ? <EyeOff size={18} /> : <Eye size={18} />}
          </button>
        )}
      </div>
      {hint && !error && <p className="mt-1.5 text-sm text-brand-textMuted">{hint}</p>}
      {error && <p className="mt-1.5 text-sm text-red-600">{error}</p>}
    </div>
  );
}
