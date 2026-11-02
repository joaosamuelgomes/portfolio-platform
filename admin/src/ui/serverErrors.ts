import type { FieldValues, Path, UseFormSetError } from 'react-hook-form';
import { ApiError, errorMessage, type FieldProblem, toFormPath } from '../api/problem';

export interface SubmitFailure {
  message: string;
  fields: FieldProblem[];
}

/**
 * Validation lives in the API; the form shows what it answered. Field errors go onto the
 * matching inputs and are also listed, so none is lost when a path has no input of its own.
 */
export function applyServerErrors<T extends FieldValues>(
  error: unknown,
  setError: UseFormSetError<T>,
  normalize: (path: string) => string = (path) => path,
): SubmitFailure {
  if (!(error instanceof ApiError)) return { message: errorMessage(error), fields: [] };
  const fields = error.fieldErrors.map((problem) => ({ ...problem, field: normalize(toFormPath(problem.field)) }));
  for (const problem of fields) {
    setError(problem.field as Path<T>, { type: 'server', message: problem.message });
  }
  if (error.status === 409 && fields.length === 0 && /slug/i.test(error.message)) {
    setError('slug' as Path<T>, { type: 'server', message: error.message });
  }
  return { message: error.message, fields };
}
