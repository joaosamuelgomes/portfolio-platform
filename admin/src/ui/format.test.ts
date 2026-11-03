import { describe, expect, it } from 'vitest';
import { slugify } from './format';

describe('slugify', () => {
  it('follows the API slug rule', () => {
    expect(slugify('  Portfólio como Produto: Full-Stack!  ')).toBe('portfolio-como-produto-full-stack');
  });

  it('collapses separators and trims hyphens at both ends', () => {
    expect(slugify('--a   b__c--')).toBe('a-b-c');
    expect(slugify('***')).toBe('');
  });

  it('cuts at 120 characters without leaving a trailing hyphen', () => {
    const slug = slugify(`${'a'.repeat(119)} b`);
    expect(slug).toBe('a'.repeat(119));
  });
});
