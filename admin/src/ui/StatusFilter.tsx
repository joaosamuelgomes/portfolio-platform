import type { StatusFilterValue } from './status';

const OPTIONS: { value: StatusFilterValue; label: string }[] = [
  { value: 'ALL', label: 'Todos' },
  { value: 'PUBLISHED', label: 'Publicados' },
  { value: 'DRAFT', label: 'Rascunhos' },
  { value: 'ARCHIVED', label: 'Arquivados' },
];

export function StatusFilter({
  value,
  counts,
  onChange,
}: {
  value: StatusFilterValue;
  counts: Record<StatusFilterValue, number>;
  onChange: (value: StatusFilterValue) => void;
}) {
  return (
    <div role="group" aria-label="Filtrar por status" className="flex flex-wrap gap-2">
      {OPTIONS.map((option) => (
        <button
          key={option.value}
          type="button"
          aria-pressed={value === option.value}
          onClick={() => onChange(option.value)}
          className={`border px-3 py-1.5 font-mono text-xs uppercase tracking-wider ${
            value === option.value ? 'border-acid text-acid' : 'border-dark-border text-zinc-400 hover:text-zinc-100'
          }`}
        >
          {option.label} <span className="text-zinc-500">{counts[option.value]}</span>
        </button>
      ))}
    </div>
  );
}

