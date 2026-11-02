import createClient from 'openapi-fetch';
import { ApiError } from './problem';
import type { components, paths } from './schema';

export type Schemas = components['schemas'];
export type Status = Schemas['ProjectResponse']['status'];
export type Role = Schemas['TokenResponse']['roles'][number];

export const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

/**
 * Typed client generated from the API's OpenAPI contract (`npm run generate:api`).
 * `fetch` is looked up on each request so tests can stub it.
 */
export const api = createClient<paths>({ baseUrl: API_URL, fetch: (request) => globalThis.fetch(request) });

// Module state rather than a React effect: children fire their queries before a provider's
// effects run, so the token has to be in place as soon as the session is known.
let accessToken: string | null = null;
let onUnauthorized = () => {};

export const setAccessToken = (token: string | null) => {
  accessToken = token;
};

export const setUnauthorizedHandler = (handler: () => void) => {
  onUnauthorized = handler;
};

api.use({
  onRequest({ request }) {
    if (accessToken) request.headers.set('Authorization', `Bearer ${accessToken}`);
    return request;
  },
  onResponse({ response }) {
    // A 401 with a token means it expired or was revoked.
    if (response.status === 401 && accessToken) onUnauthorized();
    return response;
  },
});

type Result<T> = { data?: T; error?: unknown; response: Response };

/** Resolves to the response body, or throws an {@link ApiError} with the API's problem details. */
export async function unwrap<T>(request: Promise<Result<T>>): Promise<T> {
  let result: Result<T>;
  try {
    result = await request;
  } catch {
    throw new ApiError(0, `Não foi possível falar com a API em ${API_URL}.`);
  }
  if (!result.response.ok) throw ApiError.from(result.response.status, result.error);
  return result.data as T;
}
