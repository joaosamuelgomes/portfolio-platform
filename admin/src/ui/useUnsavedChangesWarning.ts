import { useEffect } from 'react';

/** Asks before closing or reloading the tab while the form has unsaved changes. */
export function useUnsavedChangesWarning(active: boolean) {
  useEffect(() => {
    if (!active) return;
    const warn = (event: BeforeUnloadEvent) => event.preventDefault();
    window.addEventListener('beforeunload', warn);
    return () => window.removeEventListener('beforeunload', warn);
  }, [active]);
}
