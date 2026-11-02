# ADR 0003: Formato do snapshot público

- Status: aceito
- Data: 2026-11-02

## Contexto

O site é estático e gera, no build, o arquivo `src/generated/content.json` a partir de `content/projects/*.json` e `content/articles/*.md` (script `scripts/build-content.mjs`). Esse script também renderiza o Markdown com `marked`, ajusta os blocos de código para acessibilidade, calcula o tempo de leitura e monta as páginas de SEO a partir de `src/data/seo.json`. Na Fase 4 a fonte passa a ser a API.

## Decisão

`GET /public/content` devolve só conteúdo `PUBLISHED`, no formato dos arquivos de origem do site, e não no formato final do snapshot:

```json
{
  "schemaVersion": 1,
  "generatedAt": "2026-11-02T20:00:00Z",
  "projects": [
    { "id": 2, "slug": "cm-advocacia", "order": 1, "title": "...", "category": "...",
      "summary": { }, "thumbnail": "...", "logo": "...", "details": { }, "seo": { } }
  ],
  "articles": [
    { "slug": "...", "title": "...", "description": "...", "publishedAt": "2026-11-02",
      "updatedAt": "2026-11-10", "tags": [], "image": "...", "imageWidth": 1200,
      "imageHeight": 630, "imageAlt": "...", "markdown": "## ..." }
  ]
}
```

- Projetos na ordem de exibição; artigos do mais recente para o mais antigo.
- O artigo vem com o Markdown cru. Renderização, tempo de leitura, imagem padrão e páginas de SEO continuam no build do site, num lugar só.
- O projeto mantém o `id` numérico (usado em `/case/{id}`, então as URLs atuais não mudam) e ganha um `slug` único.
- Campos opcionais ausentes são omitidos, não enviados como `null`, como nos arquivos de origem.
- Datas editoriais (`publishedAt`, `updatedAt`) usam os nomes do frontmatter. No banco e nas respostas do admin, os timestamps de auditoria são outra coisa (`createdAt` e `modifiedAt` nas respostas).
- `schemaVersion` só muda em alteração incompatível; campos novos e opcionais não mudam a versão.

## Consequências

- Na Fase 4, o `build-content.mjs` troca a leitura de arquivos pela chamada à API e reaproveita todo o resto. Se a API estiver fora do ar, o build usa o último snapshot gerado.
- A API não tem lógica de apresentação nem dependência de renderizador de Markdown.
- O contrato está no OpenAPI (`ContentSnapshot`, `PublishedProject`, `PublishedArticle`), então o site pode gerar os tipos em vez de mantê-los à mão.
