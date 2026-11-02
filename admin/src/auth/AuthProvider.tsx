import { useQueryClient } from '@tanstack/react-query';
import { type ReactNode, useCallback, useEffect, useMemo, useState } from 'react';
import { api, setAccessToken, setUnauthorizedHandler, unwrap } from '../api/client';
import { AuthContext, loadSession, saveSession, type Session } from './session';

const EXPIRED = 'Sua sessão expirou. Entre de novo.';

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient();
  const [session, setSession] = useState<Session | null>(() => {
    const saved = loadSession();
    setAccessToken(saved?.token ?? null);
    return saved;
  });
  const [notice, setNotice] = useState<string | null>(null);

  const logout = useCallback(
    (reason?: string) => {
      setAccessToken(null);
      saveSession(null);
      setSession(null);
      setNotice(reason ?? null);
      queryClient.clear();
    },
    [queryClient],
  );

  const login = useCallback(async (username: string, password: string) => {
    const token = await unwrap(api.POST('/auth/token', { body: { username, password } }));
    const next = { token: token.accessToken, username, roles: token.roles, expiresAt: Date.now() + token.expiresIn * 1000 };
    setAccessToken(next.token);
    saveSession(next);
    setNotice(null);
    setSession(next);
  }, []);

  useEffect(() => {
    if (!session) return;
    setUnauthorizedHandler(() => logout(EXPIRED));
    const timer = window.setTimeout(() => logout(EXPIRED), session.expiresAt - Date.now());
    return () => window.clearTimeout(timer);
  }, [session, logout]);

  const value = useMemo(
    () => ({ session, canEdit: session?.roles.includes('ADMIN') ?? false, notice, login, logout }),
    [session, notice, login, logout],
  );
  return <AuthContext value={value}>{children}</AuthContext>;
}
