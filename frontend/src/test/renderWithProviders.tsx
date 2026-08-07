import type { ReactElement, ReactNode } from 'react';
import { render } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { I18nextProvider } from 'react-i18next';
import i18n from '../i18n';
import { AuthProvider } from '../context/AuthContext';

// Most of our pages assume they're inside a router, a react-query provider,
// i18n, and the auth context. This helper wraps a component in all of them
// so individual tests don't have to repeat the boilerplate. A fresh
// QueryClient per render keeps cached data from leaking between tests.
// initialRoute is the starting path for the in-memory router (so
// tests can render a component as if at a given URL).
export function renderWithProviders(ui: ReactElement, initialRoute: string | { pathname: string; state?: unknown } = '/') {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  });

  function Wrapper({ children }: { children: ReactNode }) {
    return (
      <I18nextProvider i18n={i18n}>
        <QueryClientProvider client={queryClient}>
          <MemoryRouter initialEntries={[initialRoute]}>
            <AuthProvider>{children}</AuthProvider>
          </MemoryRouter>
        </QueryClientProvider>
      </I18nextProvider>
    );
  }

  return render(ui, { wrapper: Wrapper });
}
