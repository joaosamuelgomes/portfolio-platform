import { Plus, Trash2 } from 'lucide-react';
import { type ReactNode, useState } from 'react';
import { type FieldPath, useFieldArray, useForm, useWatch } from 'react-hook-form';
import { Button, Input, Section, TextArea } from '../ui/controls';
import { FormErrors } from '../ui/FormErrors';
import { applyServerErrors, type SubmitFailure } from '../ui/serverErrors';
import { slugify } from '../ui/format';
import { useUnsavedChangesWarning } from '../ui/useUnsavedChangesWarning';
import type { ProjectRequest } from './api';
import { projectErrorPath, type ProjectFormValues, toRequest } from './projectValues';

interface ProjectFormProps {
  initial: ProjectFormValues;
  isNew: boolean;
  readOnly: boolean;
  onSubmit: (request: ProjectRequest) => Promise<unknown>;
}

export function ProjectForm({ initial, isNew, readOnly, onSubmit }: ProjectFormProps) {
  const form = useForm<ProjectFormValues>({ defaultValues: initial });
  const { register, control, formState, getFieldState, setValue } = form;
  const features = useFieldArray({ control, name: 'details.features' });
  const results = useFieldArray({ control, name: 'details.results' });
  const images = useFieldArray({ control, name: 'details.additionalImages' });
  const hasTestimonial = useWatch({ control, name: 'hasTestimonial' });
  const [failure, setFailure] = useState<SubmitFailure | null>(null);
  const [saved, setSaved] = useState(false);
  useUnsavedChangesWarning(formState.isDirty && !formState.isSubmitting);

  const error = (name: FieldPath<ProjectFormValues>) => getFieldState(name, formState).error?.message;
  const text = (name: FieldPath<ProjectFormValues>, label: string, extra: { hint?: string; className?: string } = {}) => (
    <Input label={label} error={error(name)} {...extra} {...register(name)} />
  );
  const long = (name: FieldPath<ProjectFormValues>, label: string, extra: { hint?: string; rows?: number } = {}) => (
    <TextArea label={label} error={error(name)} className="sm:col-span-2" {...extra} {...register(name)} />
  );

  const submit = form.handleSubmit(async (values) => {
    setFailure(null);
    setSaved(false);
    try {
      await onSubmit(toRequest(values));
      form.reset(values);
      setSaved(true);
    } catch (failed) {
      setFailure(applyServerErrors(failed, form.setError, projectErrorPath));
    }
  });

  return (
    <form onSubmit={submit} noValidate className="flex flex-col gap-6">
      <FormErrors failure={failure} />
      <fieldset disabled={readOnly} className="flex min-w-0 flex-col gap-6">
        <Section title="Identificação">
          <Input
            label="Título"
            error={error('title')}
            {...register('title', {
              onChange: (event) => {
                if (isNew && !getFieldState('slug').isDirty) setValue('slug', slugify(event.target.value));
              },
            })}
          />
          {text('slug', 'Slug', { hint: 'Minúsculas, números e hífens. Único entre os projetos.' })}
          {text('category', 'Categoria')}
          {text('thumbnail', 'Thumbnail', { hint: 'Caminho em public/ do site, ex.: /case-hero.webp' })}
          {text('logo', 'Logo (opcional)')}
        </Section>

        <Section title="Resumo na home">
          {long('summary.problem', 'Problema', { rows: 2 })}
          {long('summary.solution', 'Solução', { rows: 2 })}
          {long('summary.contribution', 'Contribuição', { rows: 2 })}
          {text('summary.proof', 'Prova', { className: 'sm:col-span-2' })}
        </Section>

        <Section title="Página do case">
          {long('details.heroSubtitle', 'Subtítulo do hero', { rows: 2 })}
          {text('details.role', 'Papel')}
          {text('details.timeline', 'Duração')}
          {text('details.platform', 'Plataforma')}
          {text('details.sector', 'Setor')}
        </Section>

        <Section title="Desafio">
          {long('details.challenge', 'Texto', { rows: 6 })}
          {long('details.painPoints', 'Dores', { rows: 5, hint: 'Uma por linha.' })}
          {text('details.challengeImage', 'Imagem')}
          {text('details.challengeImageAlt', 'Texto alternativo')}
          {text('details.challengeImageLabel', 'Legenda técnica', { className: 'sm:col-span-2' })}
        </Section>

        <Section title="Estratégia">
          {long('details.strategy', 'Texto', { rows: 6 })}
          {text('details.strategyImage', 'Imagem')}
          {text('details.strategyImageAlt', 'Texto alternativo')}
          {text('details.strategyImageLabel', 'Legenda técnica', { className: 'sm:col-span-2' })}
        </Section>

        <Section title="Solução">
          {text('details.solutionImage', 'Imagem')}
          {text('details.solutionImageAlt', 'Texto alternativo')}
          {text('details.solutionImageLabel', 'Legenda técnica', { className: 'sm:col-span-2' })}
        </Section>

        <Section
          title="Funcionalidades"
          actions={<AddButton label="Adicionar funcionalidade" onClick={() => features.append({ title: '', desc: '', image: '' })} />}
        >
          {features.fields.map((field, index) => (
            <ListItem key={field.id} label={`Funcionalidade ${index + 1}`} onRemove={() => features.remove(index)}>
              {text(`details.features.${index}.title`, 'Título')}
              {text(`details.features.${index}.image`, 'Imagem (opcional)')}
              {long(`details.features.${index}.desc`, 'Descrição', { rows: 3 })}
            </ListItem>
          ))}
        </Section>

        <Section
          title="Imagens adicionais"
          actions={<AddButton label="Adicionar imagem" onClick={() => images.append({ src: '', alt: '', label: '' })} />}
        >
          {images.fields.map((field, index) => (
            <ListItem key={field.id} label={`Imagem ${index + 1}`} onRemove={() => images.remove(index)}>
              {text(`details.additionalImages.${index}.src`, 'Caminho')}
              {text(`details.additionalImages.${index}.alt`, 'Texto alternativo')}
              {text(`details.additionalImages.${index}.label`, 'Legenda técnica', { className: 'sm:col-span-2' })}
            </ListItem>
          ))}
        </Section>

        <Section
          title="Resultados"
          actions={<AddButton label="Adicionar resultado" onClick={() => results.append({ value: '', label: '' })} />}
        >
          {results.fields.map((field, index) => (
            <ListItem key={field.id} label={`Resultado ${index + 1}`} onRemove={() => results.remove(index)}>
              {text(`details.results.${index}.value`, 'Valor')}
              {text(`details.results.${index}.label`, 'Descrição')}
            </ListItem>
          ))}
        </Section>

        <Section title="Depoimento">
          <label className="flex items-center gap-2 text-sm text-zinc-300 sm:col-span-2">
            <input type="checkbox" className="size-4 accent-acid" {...register('hasTestimonial')} />
            Este case tem depoimento
          </label>
          {hasTestimonial && (
            <>
              {long('details.testimonial.quote', 'Citação', { rows: 4 })}
              {text('details.testimonial.author', 'Autor')}
              {text('details.testimonial.role', 'Cargo')}
              {text('details.testimonial.image', 'Foto')}
              {text('details.testimonial.imageAlt', 'Texto alternativo da foto')}
              {text('details.testimonial.source', 'Fonte', { className: 'sm:col-span-2' })}
            </>
          )}
        </Section>

        <Section title="Conclusão">{long('details.conclusion', 'Texto', { rows: 5 })}</Section>

        <Section title="SEO">
          {text('seo.title', 'Título da página')}
          {text('seo.name', 'Nome (dados estruturados)')}
          {long('seo.description', 'Descrição', { rows: 2 })}
          {text('seo.headline', 'Headline')}
          {text('seo.summary', 'Resumo')}
          {text('seo.image', 'Imagem')}
          {text('seo.imageAlt', 'Texto alternativo')}
          <Input label="Largura (px)" type="number" min={1} error={error('seo.imageWidth')} {...register('seo.imageWidth', { valueAsNumber: true })} />
          <Input label="Altura (px)" type="number" min={1} error={error('seo.imageHeight')} {...register('seo.imageHeight', { valueAsNumber: true })} />
          {long('seo.about', 'Assuntos', { rows: 4, hint: 'Um por linha.' })}
        </Section>
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

function AddButton({ label, onClick }: { label: string; onClick: () => void }) {
  return (
    <Button variant="ghost" onClick={onClick}>
      <Plus size={14} aria-hidden /> {label}
    </Button>
  );
}

function ListItem({ label, onRemove, children }: { label: string; onRemove: () => void; children: ReactNode }) {
  return (
    <fieldset className="grid gap-4 border border-dark-border p-4 sm:col-span-2 sm:grid-cols-2">
      <legend className="px-2 font-mono text-[11px] uppercase tracking-wider text-zinc-500">{label}</legend>
      {children}
      <div className="flex justify-end sm:col-span-2">
        <Button variant="danger" onClick={onRemove}>
          <Trash2 size={14} aria-hidden /> Remover
        </Button>
      </div>
    </fieldset>
  );
}
