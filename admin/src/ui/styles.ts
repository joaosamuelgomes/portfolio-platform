export type ButtonVariant = 'primary' | 'secondary' | 'danger' | 'ghost';

const BUTTON_STYLES: Record<ButtonVariant, string> = {
  primary: 'border-acid bg-acid text-black hover:bg-acid/85',
  secondary: 'border-dark-border bg-dark-panel text-zinc-200 hover:border-zinc-500',
  danger: 'border-crimson/50 text-crimson hover:bg-crimson/10',
  ghost: 'border-transparent text-zinc-400 hover:text-zinc-100',
};

/** Shared by <Button> and links styled as buttons. */
export const buttonClass = (variant: ButtonVariant = 'secondary', extra = '') =>
  `inline-flex items-center justify-center gap-2 border px-3 py-2 font-mono text-xs font-bold uppercase tracking-wider transition-colors disabled:cursor-not-allowed disabled:opacity-40 ${BUTTON_STYLES[variant]} ${extra}`;
