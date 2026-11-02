import type { Schemas } from '../api/client';
import { optional, toLines } from '../ui/format';
import type { Project, ProjectRequest } from './api';

type Details = Schemas['ProjectDetails'];
type Testimonial = Schemas['CaseTestimonial'];

/**
 * The project as the form edits it: lists of strings become one-per-line text, optional
 * fields are empty strings, and the testimonial is always present behind a checkbox.
 */
export interface ProjectFormValues {
  slug: string;
  title: string;
  category: string;
  thumbnail: string;
  logo: string;
  summary: Schemas['ProjectSummary'];
  details: Omit<Details, 'painPoints' | 'features' | 'testimonial'> & {
    painPoints: string;
    features: { title: string; desc: string; image: string }[];
    testimonial: Testimonial;
  };
  hasTestimonial: boolean;
  seo: Omit<Schemas['ProjectSeo'], 'about'> & { about: string };
}

const emptyTestimonial = (): Testimonial => ({ quote: '', author: '', role: '', image: '', imageAlt: '', source: '' });

export const emptyProject = (): ProjectFormValues => ({
  slug: '',
  title: '',
  category: '',
  thumbnail: '',
  logo: '',
  summary: { problem: '', solution: '', contribution: '', proof: '' },
  details: {
    heroSubtitle: '',
    role: '',
    timeline: '',
    platform: '',
    sector: '',
    challenge: '',
    challengeImage: '',
    challengeImageAlt: '',
    challengeImageLabel: '',
    painPoints: '',
    strategy: '',
    strategyImage: '',
    strategyImageAlt: '',
    strategyImageLabel: '',
    solutionImage: '',
    solutionImageAlt: '',
    solutionImageLabel: '',
    features: [],
    additionalImages: [],
    results: [],
    testimonial: emptyTestimonial(),
    conclusion: '',
  },
  hasTestimonial: false,
  seo: {
    title: '',
    description: '',
    image: '',
    imageWidth: 1200,
    imageHeight: 630,
    imageAlt: '',
    name: '',
    headline: '',
    summary: '',
    about: '',
  },
});

export const fromProject = (project: Project): ProjectFormValues => ({
  slug: project.slug,
  title: project.title,
  category: project.category,
  thumbnail: project.thumbnail,
  logo: project.logo ?? '',
  summary: project.summary,
  details: {
    ...project.details,
    painPoints: project.details.painPoints.join('\n'),
    features: project.details.features.map((feature) => ({ ...feature, image: feature.image ?? '' })),
    testimonial: project.details.testimonial ?? emptyTestimonial(),
  },
  hasTestimonial: project.details.testimonial !== undefined,
  seo: { ...project.seo, about: project.seo.about.join('\n') },
});

export const toRequest = (values: ProjectFormValues): ProjectRequest => {
  const { painPoints, features, testimonial, ...details } = values.details;
  return {
    slug: values.slug.trim(),
    title: values.title,
    category: values.category,
    thumbnail: values.thumbnail,
    logo: optional(values.logo),
    summary: values.summary,
    details: {
      ...details,
      painPoints: toLines(painPoints),
      features: features.map((feature) => ({ title: feature.title, desc: feature.desc, image: optional(feature.image) })),
      testimonial: values.hasTestimonial ? testimonial : undefined,
    },
    seo: { ...values.seo, about: toLines(values.seo.about) },
  };
};

/** Errors on one line of a one-per-line field (`details.painPoints.2`) belong to the whole field. */
export const projectErrorPath = (path: string) => path.replace(/^(details\.painPoints|seo\.about)\.\d+$/, '$1');
