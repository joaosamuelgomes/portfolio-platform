import { describe, expect, it } from 'vitest';
import { ApiError, toFormPath } from './problem';

describe('toFormPath', () => {
  it('turns Bean Validation indexes into react-hook-form paths', () => {
    expect(toFormPath('details.features[0].title')).toBe('details.features.0.title');
    expect(toFormPath('seo.about[2]')).toBe('seo.about.2');
    expect(toFormPath('title')).toBe('title');
  });

  it('points class-level checks at the field they are about', () => {
    expect(toFormPath('markdownWithoutH1')).toBe('markdown');
    expect(toFormPath('imageSized')).toBe('image');
  });
});

describe('ApiError.from', () => {
  it('reads the problem detail and the field errors', () => {
    const error = ApiError.from(400, {
      title: 'Requisição inválida',
      detail: 'Um ou mais campos são inválidos',
      errors: [{ field: 'slug', message: 'não deve estar em branco' }],
    });
    expect(error.status).toBe(400);
    expect(error.message).toBe('Um ou mais campos são inválidos');
    expect(error.fieldErrors).toEqual([{ field: 'slug', message: 'não deve estar em branco' }]);
  });

  it('falls back to the status when the body is not a problem', () => {
    expect(ApiError.from(502, undefined).message).toBe('A API respondeu 502');
  });
});
