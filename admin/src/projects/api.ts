import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, type Schemas, unwrap } from '../api/client';

export type Project = Schemas['ProjectResponse'];
export type ProjectRequest = Schemas['ProjectRequest'];
export type ProjectAction = 'publish' | 'unpublish' | 'archive' | 'delete';

const KEY = ['projects'] as const;

/** Every project, in display order; the page filters by status locally since reordering needs all of them. */
export const useProjects = () =>
  useQuery({ queryKey: KEY, queryFn: () => unwrap(api.GET('/admin/projects')) });

export const useProject = (id: number | undefined) =>
  useQuery({
    queryKey: [...KEY, id],
    queryFn: () => unwrap(api.GET('/admin/projects/{id}', { params: { path: { id: id! } } })),
    enabled: id !== undefined,
  });

export function useSaveProject(id: number | undefined) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (body: ProjectRequest) =>
      id === undefined
        ? unwrap(api.POST('/admin/projects', { body }))
        : unwrap(api.PUT('/admin/projects/{id}', { params: { path: { id } }, body })),
    onSuccess: (saved) => {
      queryClient.setQueryData([...KEY, saved.id], saved);
      return queryClient.invalidateQueries({ queryKey: KEY, exact: true });
    },
  });
}

const runAction = (id: number, action: ProjectAction) => {
  const params = { params: { path: { id } } };
  switch (action) {
    case 'publish':
      return unwrap(api.POST('/admin/projects/{id}/publish', params));
    case 'unpublish':
      return unwrap(api.POST('/admin/projects/{id}/unpublish', params));
    case 'archive':
      return unwrap(api.POST('/admin/projects/{id}/archive', params));
    case 'delete':
      return unwrap(api.DELETE('/admin/projects/{id}', params));
  }
};

export function useProjectAction() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, action }: { id: number; action: ProjectAction }) => runAction(id, action),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: KEY }),
  });
}

export function useReorderProjects() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (projectIds: number[]) => unwrap(api.PUT('/admin/projects/order', { body: { projectIds } })),
    onSuccess: (ordered) => queryClient.setQueryData(KEY, ordered),
  });
}
