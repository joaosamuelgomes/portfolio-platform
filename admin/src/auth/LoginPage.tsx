import { type FormEvent, useState } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { errorMessage } from '../api/problem';
import { Alert, Button, Input } from '../ui/controls';
import { useAuth } from './session';

export function LoginPage() {
  const { session, notice, login } = useAuth();
  const location = useLocation();
  const [error, setError] = useState<string | null>(null);
  const [pending, setPending] = useState(false);

  if (session) {
    const from = (location.state as { from?: string } | null)?.from ?? '/projects';
    return <Navigate to={from} replace />;
  }

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    setPending(true);
    setError(null);
    try {
      await login(String(form.get('username')), String(form.get('password')));
    } catch (failure) {
      setError(errorMessage(failure));
      setPending(false);
    }
  };

  return (
    <main className="flex min-h-screen items-center justify-center px-4">
      <div className="w-full max-w-sm border border-dark-border bg-dark-panel p-8">
        <p className="font-mono text-[11px] uppercase tracking-[0.2em] text-cyan">madebyjoaogomes.com</p>
        <h1 className="mt-2 text-2xl font-bold uppercase tracking-wide text-zinc-100">
          Painel de conteúdo<span className="text-acid">_</span>
        </h1>
        <p className="mt-3 text-sm text-zinc-400">Projetos e artigos que o site publica no próximo build.</p>
        <form onSubmit={submit} className="mt-8 flex flex-col gap-4">
          {notice && <Alert tone="info">{notice}</Alert>}
          {error && <Alert>{error}</Alert>}
          <Input label="Usuário" name="username" autoComplete="username" required />
          <Input label="Senha" name="password" type="password" autoComplete="current-password" required />
          <Button type="submit" variant="primary" disabled={pending} className="mt-2">
            {pending ? 'Entrando...' : 'Entrar'}
          </Button>
        </form>
      </div>
    </main>
  );
}
