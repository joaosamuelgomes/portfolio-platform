import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, unwrap } from '../api/client';

const KEY = ['site'] as const;

export const useSiteStatus = () => useQuery({ queryKey: KEY, queryFn: () => unwrap(api.GET('/admin/site')) });

export function useDeploySite() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => unwrap(api.POST('/admin/site/deploy')),
    onSuccess: (status) => queryClient.setQueryData(KEY, status),
  });
}
