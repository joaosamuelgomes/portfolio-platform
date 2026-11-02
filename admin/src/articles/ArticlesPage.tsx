import { Plus } from 'lucide-react';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { errorMessage } from '../api/problem';
import { useAuth } from '../auth/session';
import { Alert, Button, StatusBadge } from '../ui/controls';
import { formatDate, formatDateTime } from '../ui/format';
import { countByStatus, type StatusFilterValue } from '../ui/status';
import { StatusFilter } from '../ui/StatusFilter';
import { buttonClass } from '../ui/styles';
import { type Article, type ArticleAction, useArticleAction, useArticles } from './api';

const CONFIRM: Partial<Record<ArticleAction, (article: Article) => string>> = {
  archive: (article) => `Arquivar "${article.title}"? Ele sai do site no próximo build.`,
  delete: (article) => `Excluir "${article.title}" de vez?`,
};

export function ArticlesPage() {
  const { canEdit } = useAuth();
  const articles = useArticles();
  const action = useArticleAction();
  const [filter, setFilter] = useState<StatusFilterValue>('ALL');

  const all = articles.data ?? [];
  const visible = filter === 'ALL' ? all : all.filter((article) => article.status === filter);
  const failure = action.error ?? articles.error;

  const run = (article: Article, next: ArticleAction) => {
    const question = CONFIRM[next]?.(article);
    if (question && !window.confirm(question)) return;
    action.mutate({ id: article.id, action: next });
  };

  return (
    <>
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold uppercase tracking-wide text-zinc-100">Artigos</h1>
          <p className="mt-1 text-sm text-zinc-400">Markdown que o site renderiza em /artigos. Só os publicados entram no site.</p>
        </div>
        {canEdit && (
          <Link to="/articles/new" className={buttonClass('primary')}>
            <Plus size={14} aria-hidden /> Novo artigo
          </Link>
        )}
      </div>

      <div className="mt-6 flex flex-col gap-4">
        <StatusFilter value={filter} counts={countByStatus(all)} onChange={setFilter} />
        {failure && <Alert>{errorMessage(failure)}</Alert>}
        {articles.isPending && <p className="font-mono text-sm text-zinc-500">Carregando...</p>}
        {articles.isSuccess && visible.length === 0 && (
          <p className="border border-dashed border-dark-border p-8 text-center text-sm text-zinc-500">Nenhum artigo aqui.</p>
        )}

        <ul className="flex flex-col gap-3">
          {visible.map((article) => (
            <li key={article.id} className="flex flex-col gap-4 border border-dark-border bg-dark-panel p-4 lg:flex-row lg:items-center">
              <div className="min-w-0 flex-1">
                <div className="flex flex-wrap items-center gap-2">
                  <Link to={`/articles/${article.id}`} className="font-display text-lg font-bold text-zinc-100 hover:text-acid">
                    {article.title}
                  </Link>
                  <StatusBadge status={article.status} />
                </div>
                <p className="mt-1 line-clamp-2 text-sm text-zinc-400">{article.description}</p>
                <p className="mt-1 font-mono text-[11px] text-zinc-500">
                  /artigos/{article.slug}
                  {article.publishedAt && ` · ${formatDate(article.publishedAt)}`} · alterado em {formatDateTime(article.modifiedAt)}
                </p>
              </div>
              <div className="flex flex-wrap items-center gap-2">
                <Link to={`/articles/${article.id}`} className={buttonClass('secondary')}>
                  {canEdit ? 'Editar' : 'Ver'}
                </Link>
                {canEdit && (
                  <>
                    {article.status === 'PUBLISHED' ? (
                      <Button disabled={action.isPending} onClick={() => run(article, 'unpublish')}>
                        Despublicar
                      </Button>
                    ) : (
                      <Button variant="primary" disabled={action.isPending} onClick={() => run(article, 'publish')}>
                        Publicar
                      </Button>
                    )}
                    {article.status !== 'ARCHIVED' && (
                      <Button disabled={action.isPending} onClick={() => run(article, 'archive')}>
                        Arquivar
                      </Button>
                    )}
                    <Button variant="danger" disabled={action.isPending} onClick={() => run(article, 'delete')}>
                      Excluir
                    </Button>
                  </>
                )}
              </div>
            </li>
          ))}
        </ul>
      </div>
    </>
  );
}
