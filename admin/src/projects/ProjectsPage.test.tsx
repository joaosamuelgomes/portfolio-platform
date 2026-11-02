import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { loadSession } from '../auth/session';
import { project } from '../test/fixtures';
import { mockApi, problem, renderWithSession } from '../test/render';
import { ProjectsPage } from './ProjectsPage';

const projects = [
  project(),
  project({ id: 2, slug: 'cm-advocacia', title: 'CM Advocacia', order: 2, status: 'DRAFT' }),
];

afterEach(() => vi.unstubAllGlobals());

describe('ProjectsPage', () => {
  it('lists the projects in display order with the token on the request', async () => {
    const fetch = mockApi({ 'GET /admin/projects': () => projects });
    renderWithSession(<ProjectsPage />);

    const titles = await screen.findAllByRole('link', { name: /Gelatto Sorvetes|CM Advocacia/ });
    expect(titles.map((link) => link.textContent)).toEqual(['Gelatto Sorvetes', 'CM Advocacia']);
    expect(fetch.mock.calls[0][0].headers.get('Authorization')).toBe('Bearer token');
  });

  it('moves a project and sends every id in the new order', async () => {
    const sent: unknown[] = [];
    mockApi({
      'GET /admin/projects': () => projects,
      'PUT /admin/projects/order': async (request) => {
        const body = (await request.json()) as { projectIds: number[] };
        sent.push(body);
        return body.projectIds.map((id) => projects.find((each) => each.id === id));
      },
    });
    renderWithSession(<ProjectsPage />);

    await userEvent.click(await screen.findByRole('button', { name: 'Descer Gelatto Sorvetes' }));

    await waitFor(() => expect(sent).toEqual([{ projectIds: [2, 1] }]));
  });

  it('ends the session when the API rejects the token', async () => {
    mockApi({ 'GET /admin/projects': () => problem(401, { title: 'Unauthorized' }) });
    renderWithSession(<ProjectsPage />);

    await waitFor(() => expect(loadSession()).toBeNull());
  });

  it('shows the recruiter account a read-only list', async () => {
    mockApi({ 'GET /admin/projects': () => projects });
    renderWithSession(<ProjectsPage />, { role: 'VIEWER' });

    expect(await screen.findAllByRole('link', { name: 'Ver' })).toHaveLength(2);
    expect(screen.queryByRole('link', { name: /Novo projeto/ })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Publicar|Excluir|Descer/ })).not.toBeInTheDocument();
  });
});
