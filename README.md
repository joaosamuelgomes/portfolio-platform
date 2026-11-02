# portfolio-platform

API e painel administrativo do portfólio [madebyjoaogomes.com](https://madebyjoaogomes.com). Aqui se cadastram e publicam os projetos e artigos que o site estático ([joaosamuelgomes/portfolio](https://github.com/joaosamuelgomes/portfolio)) consome no build.

## Estrutura

| Pasta | Conteúdo |
| --- | --- |
| `api/` | Java 21, Spring Boot 3, PostgreSQL + Flyway, Spring Security com JWT, OpenAPI (springdoc) |
| `admin/` | React, TypeScript, Vite, Tailwind e TanStack Query, com cliente gerado a partir do OpenAPI ([admin/README.md](admin/README.md)) |
| `scripts/` | Import do conteúdo atual do site (`content/`) para a API ([api/README.md](api/README.md#importando-o-conteúdo-do-site)) |
| `docker-compose.yml` | Sobe a API e o banco com um comando |
| `docs/adr/` | Decisões de arquitetura |

## Como se integra ao site

O site continua estático no Vercel. No build, ele busca o conteúdo publicado em um endpoint público da API e gera um snapshot no mesmo formato de `src/generated/content.json`. Se a API estiver fora do ar, o build usa o último snapshot. Publicar no painel dispara um novo deploy por Deploy Hook.

## Rodando

Veja [api/README.md](api/README.md): `cp .env.example .env`, preencha os segredos e rode `docker compose up --build`. O painel roda com `npm run dev` em `admin/` ([admin/README.md](admin/README.md)).
