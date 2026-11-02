const dateTime = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' });

export const formatDateTime = (iso: string) => dateTime.format(new Date(iso));

/** `2026-11-02` → `02/11/2026`, without going through Date (and its time zone). */
export const formatDate = (isoDate: string) => isoDate.split('-').reverse().join('/');

/** Same rule as the API and the site: lowercase letters, numbers and single hyphens. */
export const slugify = (text: string) =>
  text
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
    .slice(0, 120)
    .replace(/-+$/, '');

/** One item per non-empty line. */
export const toLines = (text: string) =>
  text
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean);

/** Optional text fields: an empty input means "absent", not an empty string. */
export const optional = (value: string | undefined) => (value?.trim() ? value.trim() : undefined);
