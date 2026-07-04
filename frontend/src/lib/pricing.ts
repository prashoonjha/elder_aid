// Placeholder fee rates until the real Payment/Stripe feature makes the
// backend the source of truth. Kept here as pure functions so both the UI
// and the tests use exactly the same math.
export const CLIENT_SERVICE_FEE_RATE = 0.08;
export const WORKER_COMMISSION_RATE = 0.12;

export interface ClientPriceBreakdown {
  price: number;
  serviceFee: number;
  total: number;
}

/**
 * What the client (family member) is shown when posting a task: the price
 * they offer, plus the platform's service fee on top.
 */
export function calculateClientBreakdown(priceOffered: number): ClientPriceBreakdown {
  const price = Number.isFinite(priceOffered) && priceOffered > 0 ? priceOffered : 0;
  const serviceFee = price * CLIENT_SERVICE_FEE_RATE;
  return {
    price,
    serviceFee,
    total: price + serviceFee,
  };
}

/**
 * What the worker is shown on a task: the offered price minus the platform
 * commission, i.e. their actual take-home.
 */
export function calculateWorkerPayout(priceOffered: number): number {
  const price = Number.isFinite(priceOffered) && priceOffered > 0 ? priceOffered : 0;
  return price - price * WORKER_COMMISSION_RATE;
}
