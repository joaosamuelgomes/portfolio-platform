import { type ComponentProps, type ReactNode, useId } from 'react';
import type { Status } from '../api/client';
import { type ButtonVariant, buttonClass } from './styles';

export function Button({ variant, className = '', ...props }: ComponentProps<'button'> & { variant?: ButtonVariant }) {
  return <button type="button" className={buttonClass(variant, className)} {...props} />;
}

const STATUS_LABEL: Record<Status, string> = { DRAFT: 'Rascunho', PUBLISHED: 'Publicado', ARCHIVED: 'Arquivado' };
const STATUS_STYLE: Record<Status, string> = {
  DRAFT: 'border-cyan/40 text-cyan',
  PUBLISHED: 'border-acid/50 text-acid',
  ARCHIVED: 'border-zinc-600 text-zinc-400',
};

export function StatusBadge({ status }: { status: Status }) {
  return (
    <span className={`inline-block border px-2 py-0.5 font-mono text-[11px] font-bold uppercase tracking-wider ${STATUS_STYLE[status]}`}>
      {STATUS_LABEL[status]}
    </span>
  );
}

export function Alert({ children, tone = 'error' }: { children: ReactNode; tone?: 'error' | 'info' }) {
  const style = tone === 'error' ? 'border-crimson/50 bg-crimson/5 text-red-200' : 'border-cyan/40 bg-cyan/5 text-cyan-100';
  return (
    <div role={tone === 'error' ? 'alert' : 'status'} className={`border px-4 py-3 text-sm ${style}`}>
      {children}
    </div>
  );
}

interface FieldProps {
  label: string;
  error?: string;
  hint?: string;
  className?: string;
}

const CONTROL =
  'w-full border bg-black/40 px-3 py-2 text-sm text-zinc-100 placeholder:text-zinc-600 focus:border-acid focus:outline-none disabled:text-zinc-400';

function FieldShell({ id, label, error, hint, className = '', children }: FieldProps & { id: string; children: ReactNode }) {
  return (
    <div className={`flex flex-col gap-1.5 ${className}`}>
      <label htmlFor={id} className="font-mono text-[11px] font-bold uppercase tracking-wider text-zinc-400">
        {label}
      </label>
      {children}
      {hint && !error && (
        <p id={`${id}-hint`} className="text-xs text-zinc-500">
          {hint}
        </p>
      )}
      {error && (
        <p id={`${id}-error`} className="text-xs text-crimson">
          {error}
        </p>
      )}
    </div>
  );
}

const describedBy = (id: string, { error, hint }: FieldProps) =>
  error ? `${id}-error` : hint ? `${id}-hint` : undefined;

export function Input({ label, error, hint, className, ...props }: FieldProps & ComponentProps<'input'>) {
  const id = useId();
  return (
    <FieldShell id={id} label={label} error={error} hint={hint} className={className}>
      <input
        id={id}
        aria-invalid={error ? true : undefined}
        aria-describedby={describedBy(id, { label, error, hint })}
        className={`${CONTROL} ${error ? 'border-crimson' : 'border-dark-border'}`}
        {...props}
      />
    </FieldShell>
  );
}

export function TextArea({ label, error, hint, className, ...props }: FieldProps & ComponentProps<'textarea'>) {
  const id = useId();
  return (
    <FieldShell id={id} label={label} error={error} hint={hint} className={className}>
      <textarea
        id={id}
        rows={4}
        aria-invalid={error ? true : undefined}
        aria-describedby={describedBy(id, { label, error, hint })}
        className={`${CONTROL} leading-relaxed ${error ? 'border-crimson' : 'border-dark-border'}`}
        {...props}
      />
    </FieldShell>
  );
}

export function Section({ title, children, actions }: { title: string; children: ReactNode; actions?: ReactNode }) {
  return (
    <section className="border border-dark-border bg-dark-panel p-5 sm:p-6">
      <div className="mb-5 flex items-center justify-between gap-4">
        <h2 className="font-mono text-xs font-bold uppercase tracking-[0.2em] text-cyan">// {title}</h2>
        {actions}
      </div>
      <div className="grid gap-4 sm:grid-cols-2">{children}</div>
    </section>
  );
}
