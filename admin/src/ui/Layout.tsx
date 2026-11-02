import { LogOut } from 'lucide-react';
import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../auth/session';
import { SiteDeployButton } from '../site/SiteDeployButton';
import { Button } from './controls';

const navClass = ({ isActive }: { isActive: boolean }) =>
  `border-b-2 px-1 py-4 font-mono text-xs font-bold uppercase tracking-wider ${
    isActive ? 'border-acid text-zinc-100' : 'border-transparent text-zinc-400 hover:text-zinc-100'
  }`;

export function Layout() {
  const { session, canEdit, logout } = useAuth();

  return (
    <div className="min-h-screen">
      <a href="#conteudo" className="sr-only focus:not-sr-only focus:absolute focus:left-4 focus:top-4 focus:bg-acid focus:px-3 focus:py-2 focus:text-black">
        Pular para o conteúdo
      </a>
      <header className="border-b border-dark-border bg-dark-panel">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-x-6 px-4 sm:px-6">
          <div className="flex items-center gap-6">
            <span className="py-4 font-display text-sm font-bold uppercase tracking-wider text-zinc-100">
              Painel<span className="text-acid">_</span>
            </span>
            <nav aria-label="Principal" className="flex gap-5">
              <NavLink to="/projects" className={navClass}>
                Projetos
              </NavLink>
              <NavLink to="/articles" className={navClass}>
                Artigos
              </NavLink>
            </nav>
          </div>
          <div className="flex flex-wrap items-center gap-3 py-2">
            {canEdit && <SiteDeployButton />}
            <span className="font-mono text-xs text-zinc-400">
              {session?.username} <span className="text-cyan">[{canEdit ? 'ADMIN' : 'VIEWER'}]</span>
            </span>
            <Button variant="ghost" onClick={() => logout()}>
              <LogOut size={14} aria-hidden /> Sair
            </Button>
          </div>
        </div>
      </header>
      {!canEdit && (
        <div className="border-b border-cyan/30 bg-cyan/5">
          <p className="mx-auto max-w-6xl px-4 py-2 font-mono text-xs text-cyan sm:px-6">
            [DEMO] Conta somente leitura: você vê tudo o que o painel faz, mas não pode alterar o conteúdo.
          </p>
        </div>
      )}
      <main id="conteudo" className="mx-auto max-w-6xl px-4 py-8 sm:px-6 sm:py-10">
        <Outlet />
      </main>
    </div>
  );
}
