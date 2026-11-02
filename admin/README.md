# portfolio-admin

Painel de conteúdo do portfólio [madebyjoaogomes.com](https://madebyjoaogomes.com): cadastra, edita, publica e ordena os projetos e artigos que a [API](../api/README.md) entrega ao build do site.

React 19 · TypeScript · Vite · Tailwind CSS 4 · TanStack Query · React Hook Form · openapi-fetch (cliente tipado gerado do OpenAPI) · Vitest e Testing Library

## Rodando

Precisa da API no ar e com uma senha definida em `ADMIN_PASSWORD` (veja [api/README.md](../api/README.md)). Na raiz do repositório:

```bash
docker compose up --build
```

Em outro terminal:

```bash
cd admin
npm install
npm run dev
```

O painel abre em http://localhost:5173, a origem que a API libera por padrão em `CORS_ALLOWED_ORIGINS`. Entre com `ADMIN_USERNAME` / `ADMIN_PASSWORD` para editar, ou com a conta `VIEWER_USERNAME` / `VIEWER_PASSWORD` para ver o modo demonstração somente leitura. Para um banco com o conteúdo atual do site, rode o [import](../api/README.md#importando-o-conteúdo-do-site).

`VITE_API_URL` (padrão `http://localhost:8080`) aponta o painel para outra API; veja `.env.example`.

## Scripts

| Comando | O que faz |
| --- | --- |
| `npm run dev` | Servidor de desenvolvimento na porta 5173 |
| `npm run build` | Checagem de tipos e build de produção em `dist/` |
| `npm test` | Testes com Vitest (jsdom) |
| `npm run lint` | ESLint, com regras de hooks e de acessibilidade (jsx-a11y) |
| `npm run generate:api` | Regenera `src/api/schema.d.ts` a partir de `/v3/api-docs` da API local |

## Como funciona

- **Contrato tipado.** `src/api/schema.d.ts` é gerado do OpenAPI da API com `openapi-typescript` e versionado, então o build não depende da API no ar. Rotas, corpos e respostas do `openapi-fetch` são checados pelo TypeScript; se a API mudar o contrato, `npm run generate:api` e o `tsc` apontam o que quebrou.
- **Validação num lugar só.** Quem valida é a API. O painel envia o formulário e mostra os erros da resposta (RFC 9457) no campo correspondente: o caminho `details.features[0].title` vira o campo `details.features.0.title` do formulário. Os erros também aparecem listados no topo, para nenhum se perder.
- **Sessão.** `POST /auth/token` devolve o JWT, que fica no `sessionStorage` (sobrevive a um reload, não ao fechar a aba) e vai em toda requisição. Quando o token expira, ou a API responde 401, o painel volta para o login com o aviso.
- **Perfis.** ADMIN edita tudo. VIEWER, a conta para recrutadores, navega pelas mesmas telas com os formulários desabilitados e sem as ações de escrita; a API também recusa essas operações para esse perfil.
- **Formulários.** O case inteiro (resumo, página, funcionalidades, imagens, resultados, depoimento e SEO) é editado em campos, não em JSON. Listas de textos usam uma linha por item, e campos opcionais vazios são enviados como ausentes. O artigo tem editor de Markdown com pré-visualização.
- **Ordem.** As setas da lista de projetos enviam a nova ordem completa para `PUT /admin/projects/order`.

## Estrutura

```
src/
├── api/        cliente openapi-fetch, tipos gerados e erros (ProblemDetail)
├── auth/       sessão, login e provider
├── projects/   lista, edição e conversão entre formulário e API
├── articles/   lista, edição com pré-visualização e conversão
├── ui/         layout, controles e utilitários compartilhados
└── test/       setup, fixtures e helpers de renderização com fetch simulado
```
