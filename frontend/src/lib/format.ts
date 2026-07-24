// Finnish uses a comma decimal separator and a space before the euro sign
// ("25,00 €"), so toFixed(2) produced the wrong format everywhere.
export function formatCurrency(amount: number, language: string): string {
  const locale = language.startsWith('en') ? 'en-GB' : 'fi-FI';
  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency: 'EUR',
    minimumFractionDigits: 2,
  }).format(amount);
}

export function formatDateTime(value: string | Date, language: string): string {
  const locale = language.startsWith('en') ? 'en-GB' : 'fi-FI';
  return new Intl.DateTimeFormat(locale, {
    day: 'numeric',
    month: 'numeric',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(typeof value === 'string' ? new Date(value) : value);
}

export function formatDate(value: string | Date, language: string): string {
  const locale = language.startsWith('en') ? 'en-GB' : 'fi-FI';
  return new Intl.DateTimeFormat(locale, {
    day: 'numeric',
    month: 'numeric',
    year: 'numeric',
  }).format(typeof value === 'string' ? new Date(value) : value);
}
