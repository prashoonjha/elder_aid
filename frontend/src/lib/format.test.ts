import { describe, it, expect } from 'vitest';
import { formatCurrency } from './format';

describe('formatCurrency', () => {
  it('uses a comma decimal separator in Finnish', () => {
    expect(formatCurrency(25, 'fi')).toContain('25,00');
  });

  it('uses a dot decimal separator in English', () => {
    expect(formatCurrency(25, 'en')).toContain('25.00');
  });

  it('always shows two decimal places', () => {
    expect(formatCurrency(7.5, 'fi')).toContain('7,50');
  });

  it('includes the euro sign', () => {
    expect(formatCurrency(10, 'fi')).toContain('€');
  });
});
