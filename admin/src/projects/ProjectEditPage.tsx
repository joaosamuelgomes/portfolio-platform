import { ArrowLeft } from 'lucide-react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { errorMessage } from '../api/problem';
import { useAuth } from '../auth/session';
import { Alert, Button, StatusBadge } from '../ui/controls';
import { formatDateTime } from '../ui/format';
import { useProject, useProjectAction, useSaveProject } from './api';
import { ProjectForm } from './ProjectForm';
import { emptyProject, fromProject } from './projectValues';

export function ProjectEditPage() {
  const { canEdit } = useAuth();
  const navigate = useNavigate();
  const params = useParams();
  const id = params.id === undefined ? undefined : Number(params.id);
  const project = useProject(id);
  const save = useSaveProject(id);
  const action = useProjectAction();

  if (id !== undefined && !project.data) {
    return project.error ? <Alert>{errorMessage(project.error)}</Alert> : <p className="font-mono text-sm text-zinc-500">Carregando...</p>;
  }
  const current = project.data;

  return (
    <>
      <Link to="/projects" className="inline-flex items-center gap-2 font-mono text-xs uppercase tracking-wider text-zinc-400 hover:text-zinc-100">
        <ArrowLeft size={14} aria-hidden /> Projetos
      </Link>
      <div className="mt-4 mb-6 flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold uppercase tracking-wide text-zinc-100">{current?.title ?? 'Novo projeto'}</h1>
          {current && (
            <p className="mt-2 flex flex-wrap items-center gap-3 font-mono text-xs text-zinc-500">
              <StatusBadge status={current.status} /> /case/{current.id} · alterado em {formatDateTime(current.modifiedAt)}
            </p>
          )}
          {!current && <p className="mt-2 text-sm text-zinc-400">O projeto nasce como rascunho, no fim da ordem.</p>}
        </div>
        {current && canEdit && (
          <Button
            variant={current.status === 'PUBLISHED' ? 'secondary' : 'primary'}
            disabled={action.isPending}
            onClick={() => action.mutate({ id: current.id, action: current.status === 'PUBLISHED' ? 'unpublish' : 'publish' })}
          >
            {current.status === 'PUBLISHED' ? 'Despublicar' : 'Publicar'}
          </Button>
        )}
      </div>
      {action.error && (
        <div className="mb-6">
          <Alert>{errorMessage(action.error)}</Alert>
        </div>
      )}
      <ProjectForm
        key={current?.id ?? 'new'}
        initial={current ? fromProject(current) : emptyProject()}
        isNew={!current}
        readOnly={!canEdit}
        onSubmit={async (request) => {
          const saved = await save.mutateAsync(request);
          if (!current) navigate(`/projects/${saved.id}`, { replace: true });
        }}
      />
    </>
  );
}
