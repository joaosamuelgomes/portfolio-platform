import DOMPurify from 'dompurify';
import { marked } from 'marked';
import { useState } from 'react';
import { type FieldPath, useForm, useWatch } from 'react-hook-form';
import { Button, Input, Section, TextArea } from '../ui/controls';
import { FormErrors } from '../ui/FormErrors';
import { applyServerErrors, type SubmitFailure } from '../ui/serverErrors';
import { slugify } from '../ui/format';
import { useUnsavedChangesWarning } from '../ui/useUnsavedChangesWarning';
import type { ArticleRequest } from './api';
import { articleErrorPath, type ArticleFormValues, toRequest } from './articleValues';

interface ArticleFormProps {
  initial: ArticleFormValues;
  isNew: boolean;
  readOnly: boolean;
  onSubmit: (request: ArticleRequest) => Promise<unknown>;
}

export function ArticleForm({ initial, isNew, readOnly, onSubmit }: ArticleFormProps) {
  const form = useForm<ArticleFormValues>({ defaultValues: initial });
  const { register, control, formState, getFieldState, setValue } = form;
  const markdown = useWatch({ control, name: 'markdown' });
  const [failure, setFailure] = useState<SubmitFailure | null>(null);
  const [saved, setSaved] = useState(false);
  const [preview, setPreview] = useState(false);
  useUnsavedChangesWarning(formState.isDirty && !formState.isSubmitting);

  const error = (name: FieldPath<ArticleFormValues>) => getFieldState(name, formState).error?.message;

  const submit = form.handleSubmit(async (values) => {
    setFailure(null);
    setSaved(false);
    try {
      await onSubmit(toRequest(values));
      form.reset(values);
      setSaved(true);
    } catch (failed) {
      setFailure(applyServerErrors(failed, form.setError, articleErrorPath));
    }
  });

  return (
    <form onSubmit={submit} noValidate className="flex flex-col gap-6">
      <FormErrors failure={failure} />
      <fieldset disabled={readOnly} className="flex min-w-0 flex-col gap-6">
        <Section title="Frontmatter">
          <Input
            label="Título"
            error={error('title')}
            {...register('title', {
              onChange: (event) => {
                if (isNew && !getFieldState('slug').isDirty) setValue('slug', slugify(event.target.value));
              },
            })}
          />
          <Input label="Slug" hint="Vira a URL /artigos/<slug>." error={error('slug')} {...register('slug')} />
          <TextArea label="Descrição" rows={2} className="sm:col-span-2" error={error('description')} {...register('description')} />
          <Input label="Tags" hint="Separadas por vírgula." className="sm:col-span-2" error={error('tags')} {...register('tags')} />
          <Input
            label="Publicado em"
            type="date"
            hint="Vazio: a data do dia em que for publicado."
            error={error('publishedAt')}
            {...register('publishedAt')}
          />
          <Input label="Atualizado em (opcional)" type="date" error={error('updatedAt')} {...register('updatedAt')} />
        </Section>

        <Section title="Imagem de compartilhamento (opcional)">
          <Input label="Imagem" hint="Sem imagem, o site usa a padrão (1200×630)." error={error('image')} {...register('image')} />
          <Input label="Texto alternativo" error={error('imageAlt')} {...register('imageAlt')} />
          <Input label="Largura (px)" type="number" min={1} error={error('imageWidth')} {...register('imageWidth', { valueAsNumber: true })} />
          <Input label="Altura (px)" type="number" min={1} error={error('imageHeight')} {...register('imageHeight', { valueAsNumber: true })} />
        </Section>

        <section className="border border-dark-border bg-dark-panel p-5 sm:p-6">
          <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
            <h2 className="font-mono text-xs font-bold uppercase tracking-[0.2em] text-cyan">{'// Corpo em Markdown'}</h2>
            <div role="group" aria-label="Modo do editor" className="flex">
              <Button variant={preview ? 'ghost' : 'secondary'} aria-pressed={!preview} onClick={() => setPreview(false)}>
                Escrever
              </Button>
              <Button variant={preview ? 'secondary' : 'ghost'} aria-pressed={preview} onClick={() => setPreview(true)}>
                Pré-visualizar
              </Button>
            </div>
          </div>
          {preview ? (
            <div
              className="markdown-preview min-h-64 border border-dark-border bg-black/40 p-5 text-zinc-300"
              // Rendered the way the site's build does, then sanitized: Markdown allows raw HTML,
              // and an article must not run script in the panel, where the session token lives.
              dangerouslySetInnerHTML={{ __html: DOMPurify.sanitize(marked.parse(markdown, { async: false })) }}
            />
          ) : (
            <TextArea
              label="Markdown"
              rows={22}
              hint='O título já é o h1 da página: comece as seções em "##".'
              className="font-mono"
              error={error('markdown')}
              {...register('markdown')}
            />
          )}
        </section>
      </fieldset>

      {!readOnly && (
        <div className="sticky bottom-0 -mx-4 flex items-center justify-end gap-4 border-t border-dark-border bg-dark/95 px-4 py-4 backdrop-blur sm:mx-0 sm:px-0">
          <p role="status" className="font-mono text-xs text-zinc-400">
            {formState.isDirty ? 'Alterações não salvas' : saved ? 'Salvo.' : ''}
          </p>
          <Button type="submit" variant="primary" disabled={formState.isSubmitting}>
            {formState.isSubmitting ? 'Salvando...' : isNew ? 'Criar rascunho' : 'Salvar'}
          </Button>
        </div>
      )}
    </form>
  );
}
