import { createContext, useContext } from 'react';
import type { Role } from '../api/client';

export interface Session {
  token: string;
  username: string;
  roles: Role[];
  /** Epoch milliseconds. */
  expiresAt: number;
}

export interface AuthState {
  session: Session | null;
  /** ADMIN writes; VIEWER (the recruiter demo account) only reads. */
  canEdit: boolean;
  /** Why the last session ended, shown on the login page. */
  notice: string | null;
  login: (username: string, password: string) => Promise<void>;
  logout: (notice?: string) => void;
}

export const AuthContext = createContext<AuthState | null>(null);

export function useAuth(): AuthState {
  const auth = useContext(AuthContext);
  if (!auth) throw new Error('useAuth precisa estar dentro de <AuthProvider>');
  return auth;
}

const STORAGE_KEY = 'portfolio-admin.session';

/** sessionStorage: the token survives a reload but not closing the tab. */
export function loadSession(now = Date.now()): Session | null {
  try {
    const session = JSON.parse(sessionStorage.getItem(STORAGE_KEY) ?? 'null') as Session | null;
    return session && session.expiresAt > now ? session : null;
  } catch {
    return null;
  }
}

export function saveSession(session: Session | null): void {
  try {
    if (session) sessionStorage.setItem(STORAGE_KEY, JSON.stringify(session));
    else sessionStorage.removeItem(STORAGE_KEY);
  } catch {
    // Storage blocked: the session just won't survive a reload.
  }
}
