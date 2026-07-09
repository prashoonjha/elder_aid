import { useState, type InputHTMLAttributes } from 'react';
import { Eye, EyeOff } from 'lucide-react';
import { useTranslation } from 'react-i18next';

interface TextFieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
}

export function TextField({ label, error, id, type, ...inputProps }: TextFieldProps) {
  const { t } = useTranslation();
  const fieldId = id ?? label.toLowerCase().replace(/\s+/g, '-');

  const isPassword = type === 'password';
  const [revealed, setRevealed] = useState(false);
  // Swap password -> text when revealed; leave any other type untouched.
  const effectiveType = isPassword && revealed ? 'text' : type;

  return (
    <div className="mb-3">
      <label htmlFor={fieldId} className="mb-1 block text-xs text-brand-textSecondary">
        {label}
      </label>
      <div className="relative">
        <input
          id={fieldId}
          type={effectiveType}
          className={`w-full rounded-field bg-brand-surface px-3 py-2.5 text-sm text-slate-900 outline-none ring-brand-accent placeholder:text-brand-textMuted focus:ring-2 ${
            isPassword ? 'pr-10' : ''
          }`}
          {...inputProps}
        />
        {isPassword && (
          <button
            type="button"
            onClick={() => setRevealed((r) => !r)}
            aria-label={revealed ? t('common.hidePassword') : t('common.showPassword')}
            className="absolute inset-y-0 right-0 flex items-center pr-3 text-brand-textMuted"
          >
            {revealed ? <EyeOff size={16} /> : <Eye size={16} />}
          </button>
        )}
      </div>
      {error && <p className="mt-1 text-xs text-red-600">{error}</p>}
    </div>
  );
}
