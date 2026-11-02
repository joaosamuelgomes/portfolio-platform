import { describe, expect, it } from 'vitest';
import { project } from '../test/fixtures';
import { emptyProject, fromProject, projectErrorPath, toRequest } from './projectValues';

describe('project form values', () => {
  it('round-trips a project without changing what the API stores', () => {
    const { slug, title, category, thumbnail, summary, details, seo } = project();
    expect(JSON.parse(JSON.stringify(toRequest(fromProject(project()))))).toEqual({ slug, title, category, thumbnail, summary, details, seo });
  });

  it('edits string lists one item per line and drops blank lines', () => {
    const values = fromProject(project());
    expect(values.details.painPoints).toBe('Dor um\nDor dois');
    values.details.painPoints = '  Primeira \n\n Segunda';
    expect(toRequest(values).details.painPoints).toEqual(['Primeira', 'Segunda']);
  });

  it('sends empty optional fields as absent', () => {
    const request = toRequest({ ...emptyProject(), logo: '  ' });
    expect(request.logo).toBeUndefined();
    expect(request.details.testimonial).toBeUndefined();
  });

  it('only sends the testimonial when the checkbox is on', () => {
    const values = fromProject(project({ details: { ...project().details, testimonial: undefined } }));
    expect(values.hasTestimonial).toBe(false);
    values.hasTestimonial = true;
    values.details.testimonial.quote = 'Ótimo';
    expect(toRequest(values).details.testimonial?.quote).toBe('Ótimo');
  });

  it('puts errors on one line of a list on the whole field', () => {
    expect(projectErrorPath('details.painPoints.1')).toBe('details.painPoints');
    expect(projectErrorPath('seo.about.0')).toBe('seo.about');
    expect(projectErrorPath('details.features.0.title')).toBe('details.features.0.title');
  });
});
