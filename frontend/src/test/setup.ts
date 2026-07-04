import '@testing-library/jest-dom/vitest';
import { afterEach } from 'vitest';
import { cleanup } from '@testing-library/react';

// Unmount anything rendered after each test so tests don't leak DOM into
// each other.
afterEach(() => {
  cleanup();
});
