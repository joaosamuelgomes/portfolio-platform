import { ArrowDown, ArrowUp, Plus } from 'lucide-react';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { errorMessage } from '../api/problem';
import { useAuth } from '../auth/session';
import { Alert, Button, StatusBadge } from '../ui/controls';
import { formatDateTime } from '../ui/format';
import { countByStatus, type StatusFilterValue } from '../ui/status';
import { StatusFilter } from '../ui/StatusFilter';
import { buttonClass } from '../ui/styles';
import { type Project, type ProjectAction, useProjectAction, useProjects, useReorderProjects } from './api';

const CONFIRM: Partial<Record<ProjectAction, (project: Project) => string>> = {
  archive: (project) => `Arquivar "${project.title}"? Ele sai do site no próximo build.`,
  delete: (project) =>
    `Excluir "${project.title}" de vez?` +
    (project.status === 'PUBLISHED' ? ` A URL /case/${project.id} deixa de existir no próximo build.` : ''),
};

export function ProjectsPage() {
  const { canEdit } = useAuth();
  const projects = useProjects();
  const action = useProjectAction();
  const reorder = useReorderProjects();
  const [filter, setFilter] = useState<StatusFilterValue>('ALL');

  const all = projects.data ?? [];
  const visible = filter === 'ALL' ? all : all.filter((project) => project.status === filter);
  const busy = action.isPending || reorder.isPending;
  const failure = action.error ?? reorder.error ?? projects.error;

  const run = (project: Project, next: ProjectAction) => {
    const question = CONFIRM[next]?.(project);
    if (question && !window.confirm(question)) return;
    action.mutate({ id: project.id, action: next });
  };

  const move = (index: number, offset: -1 | 1) => {
    const ids = all.map((project) => project.id);
    [ids[index], ids[index + offset]] = [ids[index + offset], ids[index]];
    reorder.mutate(ids);
  };

  return (
    <>
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold uppercase tracking-wide text-zinc-100">Projetos</h1>
          <p className="mt-1 text-sm text-zinc-400">Cases da home, na ordem de exibição. Só os publicados entram no site.</p>
        </div>
        {canEdit && (
          <Link to="/projects/new" className={buttonClass('primary')}>
            <Plus size={14} aria-hidden /> Novo projeto
          </Link>
        )}
      </div>

      <div className="mt-6 flex flex-col gap-4">
        <StatusFilter value={filter} counts={countByStatus(all)} onChange={setFilter} />
        {failure && <Alert>{errorMessage(failure)}</Alert>}
        {projects.isPending && <p className="font-mono text-sm text-zinc-500">Carregando...</p>}
        {projects.isSuccess && visible.length === 0 && (
          <p className="border border-dashed border-dark-border p-8 text-center text-sm text-zinc-500">Nenhum projeto aqui.</p>
        )}

        <ul className="flex flex-col gap-3">
          {visible.map((project) => {
            const index = all.indexOf(project);
            return (
              <li key={project.id} className="flex flex-col gap-4 border border-dark-border bg-dark-panel p-4 lg:flex-row lg:items-center">
                <div className="flex min-w-0 flex-1 items-start gap-4">
                  <span className="font-mono text-2xl font-bold text-zinc-600" aria-label={`Posição ${project.order}`}>
                    {String(project.order).padStart(2, '0')}
                  </span>
                  <div className="min-w-0">
                    <div className="flex flex-wrap items-center gap-2">
                      <Link to={`/projects/${project.id}`} className="font-display text-lg font-bold text-zinc-100 hover:text-acid">
                        {project.title}
                      </Link>
                      <StatusBadge status={project.status} />
                    </div>
                    <p className="mt-1 truncate text-sm text-zinc-400">{project.category}</p>
                    <p className="mt-1 font-mono text-[11px] text-zinc-500">
                      /case/{project.id} · {project.slug} · alterado em {formatDateTime(project.modifiedAt)}
                    </p>
                  </div>
                </div>

                <div className="flex flex-wrap items-center gap-2">
                  {canEdit && filter === 'ALL' && (
                    <>
                      <Button variant="ghost" aria-label={`Subir ${project.title}`} disabled={busy || index === 0} onClick={() => move(index, -1)}>
                        <ArrowUp size={14} aria-hidden />
                      </Button>
                      <Button
                        variant="ghost"
                        aria-label={`Descer ${project.title}`}
                        disabled={busy || index === all.length - 1}
                        onClick={() => move(index, 1)}
                      >
                        <ArrowDown size={14} aria-hidden />
                      </Button>
                    </>
                  )}
                  <Link to={`/projects/${project.id}`} className={buttonClass('secondary')}>
                    {canEdit ? 'Editar' : 'Ver'}
                  </Link>
                  {canEdit && (
                    <>
                      {project.status === 'PUBLISHED' ? (
                        <Button disabled={busy} onClick={() => run(project, 'unpublish')}>
                          Despublicar
                        </Button>
                      ) : (
                        <Button variant="primary" disabled={busy} onClick={() => run(project, 'publish')}>
                          Publicar
                        </Button>
                      )}
                      {project.status !== 'ARCHIVED' && (
                        <Button disabled={busy} onClick={() => run(project, 'archive')}>
                          Arquivar
                        </Button>
                      )}
                      <Button variant="danger" disabled={busy} onClick={() => run(project, 'delete')}>
                        Excluir
                      </Button>
                    </>
                  )}
                </div>
              </li>
            );
          })}
        </ul>
      </div>
    </>
  );
}
