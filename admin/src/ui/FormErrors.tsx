import { Alert } from './controls';
import type { SubmitFailure } from './serverErrors';

export function FormErrors({ failure }: { failure: SubmitFailure | null }) {
  if (!failure) return null;
  return (
    <Alert>
      <p className="font-bold">{failure.message}</p>
      {failure.fields.length > 0 && (
        <ul className="mt-2 list-inside list-disc font-mono text-xs">
          {failure.fields.map((problem) => (
            <li key={problem.field + problem.message}>
              {problem.field}: {problem.message}
            </li>
          ))}
        </ul>
      )}
    </Alert>
  );
}
