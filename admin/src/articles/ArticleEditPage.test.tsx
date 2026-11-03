import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { article } from '../test/fixtures';
import { mockApi, problem, renderWithSession } from '../test/render';
import { ArticleEditPage } from './ArticleEditPage';

afterEach(() => vi.unstubAllGlobals());

const renderEditor = (role: 'ADMIN' | 'VIEWER' = 'ADMIN') =>
  renderWithSession(<ArticleEditPage />, { role, path: '/articles/1', route: '/articles/:id' });

describe('ArticleEditPage', () => {
  it('shows the API validation errors on the fields they belong to', async () => {
    mockApi({
      'GET /admin/articles/1': () => article(),
      'PUT /admin/articles/1': () =>
        problem(400, {
          title: 'Requisição inválida',
          detail: 'Um ou mais campos são inválidos',
          errors: [
            { field: 'markdownWithoutH1', message: 'o título é o h1 da página; comece os títulos do corpo em "##"' },
            { field: 'title', message: 'não deve estar em branco' },
          ],
        }),
    });
    renderEditor();

    await userEvent.click(await screen.findByRole('button', { name: 'Salvar' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Um ou mais campos são inválidos');
    expect(screen.getByLabelText('Título')).toHaveAccessibleDescription('não deve estar em branco');
    expect(screen.getByLabelText('Markdown')).toHaveAttribute('aria-invalid', 'true');
  });

  it('saves the edited article', async () => {
    const sent: unknown[] = [];
    mockApi({
      'GET /admin/articles/1': () => article(),
      'PUT /admin/articles/1': async (request) => {
        const body = (await request.json()) as object;
        sent.push(body);
        return { ...article(), ...body };
      },
    });
    renderEditor();

    const title = await screen.findByLabelText('Título');
    await userEvent.clear(title);
    await userEvent.type(title, 'Novo título');
    await userEvent.click(screen.getByRole('button', { name: 'Salvar' }));

    expect(await screen.findByText('Salvo.')).toBeInTheDocument();
    expect(sent[0]).toMatchObject({ title: 'Novo título', slug: 'portfolio-full-stack' });
  });

  it('strips script from the Markdown preview', async () => {
    const markdown = '## Título\n\n<img src="/x.png" alt="x" onerror="alert(1)"><script>alert(2)</script>';
    mockApi({ 'GET /admin/articles/1': () => article({ markdown }) });
    renderEditor();

    await userEvent.click(await screen.findByRole('button', { name: 'Pré-visualizar' }));

    const image = await screen.findByRole('img', { name: 'x' });
    expect(image).not.toHaveAttribute('onerror');
    expect(document.querySelector('.markdown-preview script')).toBeNull();
  });

  it('opens read-only for the recruiter account', async () => {
    mockApi({ 'GET /admin/articles/1': () => article() });
    renderEditor('VIEWER');

    expect(await screen.findByLabelText('Título')).toBeDisabled();
    expect(screen.queryByRole('button', { name: /Salvar|Publicar/ })).not.toBeInTheDocument();
  });
});
