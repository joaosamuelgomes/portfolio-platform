import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render } from '@testing-library/react';
import type { ReactNode } from 'react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { vi } from 'vitest';
import type { Role } from '../api/client';
import { AuthProvider } from '../auth/AuthProvider';
import { saveSession } from '../auth/session';

type Handler = (request: Request) => unknown;

/** Stubs fetch with `"METHOD /path"` handlers; a handler's return value is sent as JSON. */
export function mockApi(handlers: Record<string, Handler>) {
  const fetch = vi.fn(async (request: Request) => {
    const key = `${request.method} ${new URL(request.url).pathname}`;
    const handler = handlers[key];
    if (!handler) return new Response(JSON.stringify({ title: `sem mock para ${key}` }), { status: 500 });
    const result = await handler(request.clone());
    return result instanceof Response ? result : Response.json(result);
  });
  vi.stubGlobal('fetch', fetch);
  return fetch;
}

export const problem = (status: number, body: object) =>
  new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/problem+json' } });

export function renderWithSession(ui: ReactNode, { role = 'ADMIN', path = '/', route = '/' }: { role?: Role; path?: string; route?: string } = {}) {
  saveSession({ token: 'token', username: role === 'ADMIN' ? 'admin' : 'recrutador', roles: [role], expiresAt: Date.now() + 3_600_000 });
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={[path]}>
        <AuthProvider>
          <Routes>
            <Route path={route} element={ui} />
          </Routes>
        </AuthProvider>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}
