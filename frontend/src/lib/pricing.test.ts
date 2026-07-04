import { describe, it, expect } from 'vitest';
import { calculateClientBreakdown, calculateWorkerPayout } from './pricing';

describe('calculateClientBreakdown', () => {
  it('adds an 8% service fee on top of the offered price', () => {
    const result = calculateClientBreakdown(25);
    expect(result.price).toBe(25);
    expect(result.serviceFee).toBeCloseTo(2, 5);
    expect(result.total).toBeCloseTo(27, 5);
  });

  it('treats a zero price as no fee and no total', () => {
    const result = calculateClientBreakdown(0);
    expect(result).toEqual({ price: 0, serviceFee: 0, total: 0 });
  });

  it('treats a negative or NaN price as zero rather than producing junk', () => {
    expect(calculateClientBreakdown(-10).total).toBe(0);
    expect(calculateClientBreakdown(NaN).total).toBe(0);
  });
});

describe('calculateWorkerPayout', () => {
  it('deducts a 12% commission from the offered price', () => {
    expect(calculateWorkerPayout(25)).toBeCloseTo(22, 5);
  });

  it('returns zero for a zero or invalid price', () => {
    expect(calculateWorkerPayout(0)).toBe(0);
    expect(calculateWorkerPayout(NaN)).toBe(0);
  });
});
