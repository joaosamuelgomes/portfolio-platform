#!/usr/bin/env node
// Imports the site's current content (content/projects/*.json and content/articles/*.md) into an
// empty API database, through the admin endpoints, keeping the project ids used in /case/{id}.
//
// The API generates ids, so the projects are created in id order on an empty database and each
// returned id is checked; that is why the ids must be 1..n and the script refuses to run when the
// database already has content. At the end it compares GET /public/content with the files.
//
// Usage: node import-content.mjs [--content <dir>] [--api <url>] [--dry-run]

import { existsSync } from 'node:fs';
import { readdir, readFile } from 'node:fs/promises';
import { basename, dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { isDeepStrictEqual, parseArgs } from 'node:util';
import matter from 'gray-matter';

const scriptDir = dirname(fileURLToPath(import.meta.url));
const envFile = resolve(scriptDir, '..', '.env');
const SLUG_PATTERN = /^[a-z0-9]+(-[a-z0-9]+)*$/;

class ImportError extends Error {}

const fail = (message) => {
    throw new ImportError(message);
};

const { values: options } = parseArgs({
    options: {
        content: { type: 'string', default: process.env.CONTENT_DIR ?? resolve(scriptDir, '..', '..', 'portfolio', 'content') },
        api: { type: 'string', default: process.env.API_URL ?? 'http://localhost:8080' },
        'dry-run': { type: 'boolean', default: false },
    },
});

const toIsoDate = (file, value, field) => {
    const date = value instanceof Date ? value : new Date(String(value));
    if (Number.isNaN(date.getTime())) fail(`${file}: "${field}" precisa ser uma data (YYYY-MM-DD)`);
    return date.toISOString().slice(0, 10);
};

const listFiles = async (folder, extension) => {
    const entries = await readdir(resolve(options.content, folder)).catch((error) => {
        if (error.code === 'ENOENT') fail(`pasta não encontrada: ${resolve(options.content, folder)} (use --content)`);
        throw error;
    });
    return entries.filter((name) => name.endsWith(extension)).sort();
};

/** Projects in id order, each with the API request body plus what the request leaves out. */
const readProjects = async () => {
    const projects = [];
    for (const name of await listFiles('projects', '.json')) {
        const file = `projects/${name}`;
        const { id, published, order, ...fields } = JSON.parse(await readFile(resolve(options.content, 'projects', name), 'utf8'));
        const slug = basename(name, '.json');
        if (!Number.isInteger(id)) fail(`${file}: "id" precisa ser um inteiro`);
        if (typeof published !== 'boolean') fail(`${file}: "published" precisa ser true ou false`);
        if (!Number.isInteger(order)) fail(`${file}: "order" precisa ser um inteiro`);
        if (!SLUG_PATTERN.test(slug)) fail(`${file}: o nome do arquivo vira o slug e precisa ter só minúsculas, números e hífens`);
        projects.push({ file, id, published, order, request: { slug, ...fields } });
    }

    projects.sort((first, second) => first.id - second.id);
    projects.forEach((project, index) => {
        if (project.id !== index + 1) {
            fail(`os ids dos projetos precisam ser 1..${projects.length} sem lacunas (a API gera o id em sequência); `
                + `encontrei ${projects.map((each) => each.id).join(', ')}`);
        }
    });
    return projects;
};

const readArticles = async () => {
    const articles = [];
    for (const name of await listFiles('articles', '.md')) {
        const file = `articles/${name}`;
        const slug = basename(name, '.md');
        if (!SLUG_PATTERN.test(slug)) fail(`${file}: o nome do arquivo vira o slug e precisa ter só minúsculas, números e hífens`);

        const { data, content } = matter(await readFile(resolve(options.content, 'articles', name), 'utf8'));
        if (typeof data.published !== 'boolean') fail(`${file}: "published" precisa ser true ou false`);
        articles.push({
            file,
            published: data.published,
            request: {
                slug,
                title: data.title,
                description: data.description,
                markdown: content.replace(/^\s*\n/, ''),
                tags: data.tags ?? [],
                image: data.image,
                imageWidth: data.imageWidth,
                imageHeight: data.imageHeight,
                imageAlt: data.imageAlt,
                publishedAt: data.publishedAt === undefined ? undefined : toIsoDate(file, data.publishedAt, 'publishedAt'),
                updatedAt: data.updatedAt === undefined ? undefined : toIsoDate(file, data.updatedAt, 'updatedAt'),
            },
        });
    }
    return articles;
};

const api = async (method, path, { token, body } = {}) => {
    const response = await fetch(new URL(path, options.api), {
        method,
        headers: {
            ...(body === undefined ? {} : { 'Content-Type': 'application/json' }),
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        body: body === undefined ? undefined : JSON.stringify(body),
    }).catch((error) => fail(`não consegui falar com a API em ${options.api} (${error.cause?.code ?? error.message}); ela está no ar?`));

    if (!response.ok) {
        const problem = await response.json().catch(() => ({}));
        const details = (problem.errors ?? []).map((error) => `\n  - ${error.field}: ${error.message}`).join('');
        fail(`${method} ${path} respondeu ${response.status}: ${problem.detail ?? problem.title ?? response.statusText}${details}`);
    }
    return response.status === 204 ? undefined : response.json();
};

const withoutUndefined = (value) => JSON.parse(JSON.stringify(value));

const verify = async (token, projects, articles) => {
    const snapshot = await api('GET', '/public/content');
    const problems = [];

    const expectedProjects = projects.filter((project) => project.published).sort((first, second) => first.order - second.order);
    const ids = (list) => list.map((project) => project.id).join(', ');
    if (ids(snapshot.projects) !== ids(expectedProjects)) {
        problems.push(`projetos publicados: esperava ids [${ids(expectedProjects)}] nessa ordem, veio [${ids(snapshot.projects)}]`);
    }
    for (const project of expectedProjects) {
        const actual = snapshot.projects.find((each) => each.id === project.id);
        const { order: _order, ...comparable } = actual ?? {};
        if (actual && !isDeepStrictEqual(comparable, withoutUndefined({ id: project.id, ...project.request }))) {
            problems.push(`${project.file}: o conteúdo devolvido pela API difere do arquivo (algum campo não é aceito pela API?)`);
        }
    }

    for (const article of articles) {
        const actual = snapshot.articles.find((each) => each.slug === article.request.slug);
        if (!article.published) {
            if (actual) problems.push(`${article.file}: é rascunho mas aparece em /public/content`);
            continue;
        }
        if (!actual) {
            problems.push(`${article.file}: não aparece em /public/content`);
        } else if (!isDeepStrictEqual(actual, withoutUndefined(article.request))) {
            problems.push(`${article.file}: o conteúdo devolvido pela API difere do arquivo`);
        }
    }

    const drafts = await api('GET', '/admin/articles?status=DRAFT', { token });
    for (const article of articles.filter((each) => !each.published)) {
        if (!drafts.some((each) => each.slug === article.request.slug)) problems.push(`${article.file}: não ficou como DRAFT`);
    }

    if (problems.length > 0) fail(`a verificação encontrou diferenças:\n  - ${problems.join('\n  - ')}`);
    return snapshot;
};

const main = async () => {
    if (existsSync(envFile)) process.loadEnvFile(envFile);
    const projects = await readProjects();
    const articles = await readArticles();

    console.log(`Conteúdo em ${options.content}:`);
    for (const project of projects) {
        console.log(`  projeto #${project.id} ${project.request.slug} (ordem ${project.order}, ${project.published ? 'publicado' : 'rascunho'})`);
    }
    for (const article of articles) {
        console.log(`  artigo ${article.request.slug} (${article.published ? 'publicado' : 'rascunho'})`);
    }
    if (options['dry-run']) {
        console.log('--dry-run: nada foi enviado.');
        return;
    }

    const username = process.env.ADMIN_USERNAME || 'admin';
    const password = process.env.ADMIN_PASSWORD || fail(`defina ADMIN_PASSWORD (no ambiente ou em ${envFile})`);
    const { accessToken: token } = await api('POST', '/auth/token', { body: { username, password } });

    const [existingProjects, existingArticles] = await Promise.all([
        api('GET', '/admin/projects', { token }),
        api('GET', '/admin/articles', { token }),
    ]);
    if (existingProjects.length > 0 || existingArticles.length > 0) {
        fail(`o banco já tem ${existingProjects.length} projeto(s) e ${existingArticles.length} artigo(s); o import só roda num banco vazio, `
            + 'porque os ids dos projetos dependem da sequência. Para recomeçar do zero: docker compose down -v');
    }

    console.log(`Importando para ${options.api}...`);
    const created = [];
    for (const project of projects) {
        const { id } = await api('POST', '/admin/projects', { token, body: project.request });
        created.push(id);
        if (id !== project.id) {
            // The identity sequence was already used (rows created and deleted): undo and stop.
            for (const createdId of created) await api('DELETE', `/admin/projects/${createdId}`, { token });
            fail(`${project.file}: a API gerou o id ${id} em vez de ${project.id} (a sequência do banco já foi usada); `
                + 'os projetos criados foram removidos. Recomece com um banco novo: docker compose down -v');
        }
    }

    const byOrder = [...projects].sort((first, second) => first.order - second.order || first.id - second.id);
    if (byOrder.some((project, index) => project.id !== projects[index].id)) {
        await api('PUT', '/admin/projects/order', { token, body: { projectIds: byOrder.map((project) => project.id) } });
    }
    for (const project of projects.filter((each) => each.published)) {
        await api('POST', `/admin/projects/${project.id}/publish`, { token });
    }

    for (const article of articles) {
        const { id } = await api('POST', '/admin/articles', { token, body: article.request });
        if (article.published) await api('POST', `/admin/articles/${id}/publish`, { token });
    }

    // verify() throws if /public/content differs from the files, so the counts below are the API's too.
    // Only local values go to the log: nothing the API returned is echoed into it.
    await verify(token, projects, articles);
    console.log(`Pronto: ${projects.length} projeto(s) e ${articles.length} artigo(s) importados e conferidos em /public/content.`);
};

main().catch((error) => {
    console.error(error instanceof ImportError ? `Erro: ${error.message}` : error);
    process.exitCode = 1;
});
