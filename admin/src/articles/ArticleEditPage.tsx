import { ArrowLeft } from 'lucide-react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { errorMessage } from '../api/problem';
import { useAuth } from '../auth/session';
import { Alert, Button, StatusBadge } from '../ui/controls';
import { formatDateTime } from '../ui/format';
import { useArticle, useArticleAction, useSaveArticle } from './api';
import { ArticleForm } from './ArticleForm';
import { emptyArticle, fromArticle } from './articleValues';

export function ArticleEditPage() {
  const { canEdit } = useAuth();
  const navigate = useNavigate();
  const params = useParams();
  const id = params.id === undefined ? undefined : Number(params.id);
  const article = useArticle(id);
  const save = useSaveArticle(id);
  const action = useArticleAction();

  if (id !== undefined && !article.data) {
    return article.error ? <Alert>{errorMessage(article.error)}</Alert> : <p className="font-mono text-sm text-zinc-500">Carregando...</p>;
  }
  const current = article.data;

  return (
    <>
      <Link to="/articles" className="inline-flex items-center gap-2 font-mono text-xs uppercase tracking-wider text-zinc-400 hover:text-zinc-100">
        <ArrowLeft size={14} aria-hidden /> Artigos
      </Link>
      <div className="mt-4 mb-6 flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold uppercase tracking-wide text-zinc-100">{current?.title ?? 'Novo artigo'}</h1>
          {current && (
            <p className="mt-2 flex flex-wrap items-center gap-3 font-mono text-xs text-zinc-500">
              <StatusBadge status={current.status} /> /artigos/{current.slug} · alterado em {formatDateTime(current.modifiedAt)}
            </p>
          )}
          {!current && <p className="mt-2 text-sm text-zinc-400">O artigo nasce como rascunho.</p>}
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
      <ArticleForm
        key={current?.id ?? 'new'}
        initial={current ? fromArticle(current) : emptyArticle()}
        isNew={!current}
        readOnly={!canEdit}
        onSubmit={async (request) => {
          const saved = await save.mutateAsync(request);
          if (!current) navigate(`/articles/${saved.id}`, { replace: true });
        }}
      />
    </>
  );
}
