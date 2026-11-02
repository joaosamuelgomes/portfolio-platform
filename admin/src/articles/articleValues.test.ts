import { describe, expect, it } from 'vitest';
import { article } from '../test/fixtures';
import { emptyArticle, fromArticle, toRequest } from './articleValues';

describe('article form values', () => {
  it('edits tags as comma-separated text', () => {
    const values = fromArticle(article());
    expect(values.tags).toBe('React, Spring Boot');
    values.tags = ' Java ,, Spring Boot ,';
    expect(toRequest(values).tags).toEqual(['Java', 'Spring Boot']);
  });

  it('sends empty image fields and dates as absent', () => {
    const request = toRequest({ ...emptyArticle(), title: 'T' });
    expect(request.image).toBeUndefined();
    expect(request.imageWidth).toBeUndefined();
    expect(request.imageHeight).toBeUndefined();
    expect(request.publishedAt).toBeUndefined();
    expect(request.updatedAt).toBeUndefined();
  });

  it('keeps the editorial dates as plain ISO dates', () => {
    const request = toRequest(fromArticle(article({ updatedAt: '2026-11-10' })));
    expect(request.publishedAt).toBe('2026-11-02');
    expect(request.updatedAt).toBe('2026-11-10');
  });
});
