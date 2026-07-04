import type { ReactElement, ReactNode } from "react";
import { render } from "@testing-library/react";
import { AuthProvider } from "../context/AuthContext";
import { MemoryRouter, type InitialEntry } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { I18nextProvider } from "react-i18next";
import i18n from "../i18n";

// Most of our pages assume they're inside a router, a react-query provider,
// and i18n. This helper wraps a component in all three so individual tests
// don't have to repeat the boilerplate. A fresh QueryClient per render keeps
// cached data from leaking between tests. initialRoute accepts either a path
// string or a full location object (so tests can pass router state).
export function renderWithProviders(
  ui: ReactElement,
  initialRoute: InitialEntry = "/",
) {
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
