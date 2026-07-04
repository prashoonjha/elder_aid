import { describe, it, expect } from 'vitest';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Routes, Route } from 'react-router-dom';
import { RegisterPage } from '../pages/RegisterPage';
import { renderWithProviders } from '../test/renderWithProviders';
import i18n from '../i18n';

// RegisterPage redirects away if there's no role in router state, so we
// render it inside a route tree and navigate to it with state via a small
// launcher. Easiest path: use MemoryRouter's initial entry with state.
function renderRegisterWithRole() {
  return renderWithProviders(
    <Routes>
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/" element={<div>redirected home</div>} />
    </Routes>,
    { pathname: '/register', state: { role: 'WORKER' } },
  );
}

describe('RegisterPage password strength hint', () => {
  it('shows no hint before the user types anything', async () => {
    i18n.changeLanguage('en');
    renderRegisterWithRole();

    expect(screen.queryByText('At least 10 characters')).not.toBeInTheDocument();
    expect(screen.queryByText('Password length looks good')).not.toBeInTheDocument();
  });

  it('shows the too-short hint for a short password', async () => {
    i18n.changeLanguage('en');
    renderRegisterWithRole();

    const passwordField = screen.getByLabelText('Password');
    await userEvent.type(passwordField, 'short');

    expect(screen.getByText('At least 10 characters')).toBeInTheDocument();
  });

  it('switches to the positive hint once the password reaches 10 characters', async () => {
    i18n.changeLanguage('en');
    renderRegisterWithRole();

    const passwordField = screen.getByLabelText('Password');
    await userEvent.type(passwordField, 'tenchars10');

    expect(screen.getByText('Password length looks good')).toBeInTheDocument();
    expect(screen.queryByText('At least 10 characters')).not.toBeInTheDocument();
  });
});
