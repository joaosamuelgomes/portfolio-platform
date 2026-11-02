import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { mockApi, problem, renderWithSession } from '../test/render';
import { SiteDeployButton } from './SiteDeployButton';

afterEach(() => vi.unstubAllGlobals());

describe('SiteDeployButton', () => {
  it('asks the API for a site build after confirmation', async () => {
    vi.stubGlobal('confirm', () => true);
    const deploy = vi.fn(() => ({ deployConfigured: true, lastDeployRequestedAt: '2026-11-02T21:30:00Z' }));
    mockApi({ 'GET /admin/site': () => ({ deployConfigured: true }), 'POST /admin/site/deploy': deploy });
    renderWithSession(<SiteDeployButton />);

    const button = await screen.findByRole('button', { name: /Atualizar site/ });
    await waitFor(() => expect(button).toBeEnabled());
    await userEvent.click(button);

    expect(await screen.findByRole('status')).toHaveTextContent(/Build pedido às/);
    expect(deploy).toHaveBeenCalledOnce();
  });

  it('does nothing when the confirmation is cancelled', async () => {
    vi.stubGlobal('confirm', () => false);
    const deploy = vi.fn();
    mockApi({ 'GET /admin/site': () => ({ deployConfigured: true }), 'POST /admin/site/deploy': deploy });
    renderWithSession(<SiteDeployButton />);

    const button = await screen.findByRole('button', { name: /Atualizar site/ });
    await waitFor(() => expect(button).toBeEnabled());
    await userEvent.click(button);

    expect(deploy).not.toHaveBeenCalled();
  });

  it('stays disabled while the API has no Deploy Hook', async () => {
    mockApi({ 'GET /admin/site': () => ({ deployConfigured: false }) });
    renderWithSession(<SiteDeployButton />);

    expect(await screen.findByRole('status')).toHaveTextContent('Deploy Hook não configurado na API');
    expect(screen.getByRole('button', { name: /Atualizar site/ })).toBeDisabled();
  });

  it('shows why the hook failed', async () => {
    vi.stubGlobal('confirm', () => true);
    mockApi({
      'GET /admin/site': () => ({ deployConfigured: true }),
      'POST /admin/site/deploy': () => problem(502, { title: 'Falha ao atualizar o site', detail: 'O Deploy Hook do site não aceitou o pedido' }),
    });
    renderWithSession(<SiteDeployButton />);

    const button = await screen.findByRole('button', { name: /Atualizar site/ });
    await waitFor(() => expect(button).toBeEnabled());
    await userEvent.click(button);

    expect(await screen.findByRole('status')).toHaveTextContent('O Deploy Hook do site não aceitou o pedido');
  });
});
