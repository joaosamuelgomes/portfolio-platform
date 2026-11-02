import { Rocket } from 'lucide-react';
import { errorMessage } from '../api/problem';
import { Button } from '../ui/controls';
import { useDeploySite, useSiteStatus } from './api';

const time = new Intl.DateTimeFormat('pt-BR', { timeStyle: 'short' });

/**
 * Publishing in the panel only changes what GET /public/content returns; the static site picks
 * it up on its next build. This button asks for that build, once for a whole batch of edits.
 */
export function SiteDeployButton() {
  const status = useSiteStatus();
  const deploy = useDeploySite();

  const configured = status.data?.deployConfigured ?? false;
  const last = status.data?.lastDeployRequestedAt;
  const message = deploy.error
    ? errorMessage(deploy.error)
    : last
      ? `Build pedido às ${time.format(new Date(last))}`
      : configured
        ? null
        : 'Deploy Hook não configurado na API';

  const run = () => {
    if (window.confirm('Gerar o site de novo com o conteúdo publicado agora? Leva alguns minutos.')) deploy.mutate();
  };

  return (
    <div className="flex items-center gap-3">
      {message && (
        <span role="status" className={`hidden font-mono text-[11px] md:inline ${deploy.error ? 'text-crimson' : 'text-zinc-500'}`}>
          {message}
        </span>
      )}
      <Button variant="primary" disabled={!configured || deploy.isPending} title={configured ? undefined : message ?? undefined} onClick={run}>
        <Rocket size={14} aria-hidden /> {deploy.isPending ? 'Pedindo...' : 'Atualizar site'}
      </Button>
    </div>
  );
}
