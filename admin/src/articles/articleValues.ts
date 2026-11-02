import { optional } from '../ui/format';
import type { Article, ArticleRequest } from './api';

/** The article as the form edits it: tags as comma-separated text, absent values as empty strings. */
export interface ArticleFormValues {
  slug: string;
  title: string;
  description: string;
  markdown: string;
  tags: string;
  image: string;
  imageWidth: number;
  imageHeight: number;
  imageAlt: string;
  publishedAt: string;
  updatedAt: string;
}

export const emptyArticle = (): ArticleFormValues => ({
  slug: '',
  title: '',
  description: '',
  markdown: '',
  tags: '',
  image: '',
  imageWidth: Number.NaN,
  imageHeight: Number.NaN,
  imageAlt: '',
  publishedAt: '',
  updatedAt: '',
});

export const fromArticle = (article: Article): ArticleFormValues => ({
  slug: article.slug,
  title: article.title,
  description: article.description,
  markdown: article.markdown,
  tags: article.tags.join(', '),
  image: article.image ?? '',
  imageWidth: article.imageWidth ?? Number.NaN,
  imageHeight: article.imageHeight ?? Number.NaN,
  imageAlt: article.imageAlt ?? '',
  publishedAt: article.publishedAt ?? '',
  updatedAt: article.updatedAt ?? '',
});

const optionalNumber = (value: number) => (Number.isFinite(value) ? value : undefined);

export const toRequest = (values: ArticleFormValues): ArticleRequest => ({
  slug: values.slug.trim(),
  title: values.title,
  description: values.description,
  markdown: values.markdown,
  tags: values.tags
    .split(',')
    .map((tag) => tag.trim())
    .filter(Boolean),
  image: optional(values.image),
  imageWidth: optionalNumber(values.imageWidth),
  imageHeight: optionalNumber(values.imageHeight),
  imageAlt: optional(values.imageAlt),
  publishedAt: optional(values.publishedAt),
  updatedAt: optional(values.updatedAt),
});

/** Errors on one tag (`tags.1`) belong to the tags field. */
export const articleErrorPath = (path: string) => path.replace(/^tags\.\d+$/, 'tags');
