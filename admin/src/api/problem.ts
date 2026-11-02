export interface FieldProblem {
  field: string;
  message: string;
}

interface ProblemBody {
  title?: string;
  detail?: string;
  errors?: FieldProblem[];
}

/** An RFC 9457 problem from the API, or a network failure (status 0). */
export class ApiError extends Error {
  readonly status: number;
  readonly fieldErrors: FieldProblem[];

  constructor(status: number, message: string, fieldErrors: FieldProblem[] = []) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.fieldErrors = fieldErrors;
  }

  static from(status: number, body: unknown): ApiError {
    const problem = (typeof body === 'object' && body !== null ? body : {}) as ProblemBody;
    const message = problem.detail ?? problem.title ?? `A API respondeu ${status}`;
    return new ApiError(status, message, Array.isArray(problem.errors) ? problem.errors : []);
  }
}

/** Class-level checks the API reports under the name of their getter. */
const CHECK_TO_FIELD: Record<string, string> = {
  markdownWithoutH1: 'markdown',
  imageSized: 'image',
};

/** Bean Validation paths (`details.features[0].title`) to react-hook-form paths (`details.features.0.title`). */
export const toFormPath = (field: string): string =>
  CHECK_TO_FIELD[field] ?? field.replace(/\[(\d+)\]/g, '.$1');

export const errorMessage = (error: unknown): string =>
  error instanceof Error ? error.message : 'Algo deu errado.';
