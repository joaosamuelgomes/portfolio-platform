import type { Status } from '../api/client';

export type StatusFilterValue = Status | 'ALL';

export const countByStatus = (items: { status: Status }[]): Record<StatusFilterValue, number> => ({
  ALL: items.length,
  PUBLISHED: items.filter((item) => item.status === 'PUBLISHED').length,
  DRAFT: items.filter((item) => item.status === 'DRAFT').length,
  ARCHIVED: items.filter((item) => item.status === 'ARCHIVED').length,
});
