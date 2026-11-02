import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, type Schemas, unwrap } from '../api/client';

export type Article = Schemas['ArticleResponse'];
export type ArticleRequest = Schemas['ArticleRequest'];
export type ArticleAction = 'publish' | 'unpublish' | 'archive' | 'delete';

const KEY = ['articles'] as const;

/** Every article, newest first. */
export const useArticles = () =>
  useQuery({ queryKey: KEY, queryFn: () => unwrap(api.GET('/admin/articles')) });

export const useArticle = (id: number | undefined) =>
  useQuery({
    queryKey: [...KEY, id],
    queryFn: () => unwrap(api.GET('/admin/articles/{id}', { params: { path: { id: id! } } })),
    enabled: id !== undefined,
  });

export function useSaveArticle(id: number | undefined) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (body: ArticleRequest) =>
      id === undefined
        ? unwrap(api.POST('/admin/articles', { body }))
        : unwrap(api.PUT('/admin/articles/{id}', { params: { path: { id } }, body })),
    onSuccess: (saved) => {
      queryClient.setQueryData([...KEY, saved.id], saved);
      return queryClient.invalidateQueries({ queryKey: KEY, exact: true });
    },
  });
}

const runAction = (id: number, action: ArticleAction) => {
  const params = { params: { path: { id } } };
  switch (action) {
    case 'publish':
      return unwrap(api.POST('/admin/articles/{id}/publish', params));
    case 'unpublish':
      return unwrap(api.POST('/admin/articles/{id}/unpublish', params));
    case 'archive':
      return unwrap(api.POST('/admin/articles/{id}/archive', params));
    case 'delete':
      return unwrap(api.DELETE('/admin/articles/{id}', params));
  }
};

export function useArticleAction() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, action }: { id: number; action: ArticleAction }) => runAction(id, action),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: KEY }),
  });
}
