# portfolio-platform

API e painel administrativo do portfólio [madebyjoaogomes.com](https://madebyjoaogomes.com). Aqui se cadastram e publicam os projetos e artigos que o site estático ([joaosamuelgomes/portfolio](https://github.com/joaosamuelgomes/portfolio)) consome no build.

## Estrutura

| Pasta | Conteúdo |
| --- | --- |
| `api/` | Java 21, Spring Boot 3, PostgreSQL + Flyway, Spring Security com JWT, OpenAPI (springdoc) |
| `admin/` | React, TypeScript, Vite, Tailwind e TanStack Query, com cliente gerado a partir do OpenAPI ([admin/README.md](admin/README.md)) |
| `scripts/` | Import do conteúdo atual do site (`content/`) para a API ([api/README.md](api/README.md#importando-o-conteúdo-do-site)) |
| `docker-compose.yml` | Sobe a API e o banco com um comando (desenvolvimento) |
| `deploy/` | Produção: VM Oracle Cloud com Caddy, backup e script de deploy ([deploy/README.md](deploy/README.md)) |
| `docs/adr/` | Decisões de arquitetura |

## Como se integra ao site

O site continua estático no Vercel. No build, com `CONTENT_API_URL` definida, ele lê o conteúdo publicado em `GET /public/content` e gera o mesmo snapshot que antes vinha da pasta `content/`. Se a API não responder, o build falha e o deploy atual continua no ar. Depois de publicar no painel, o botão "Atualizar site" chama a API, que aciona o Deploy Hook do Vercel ([ADR 0004](docs/adr/0004-site-le-a-api-no-build.md)).

## Rodando

Veja [api/README.md](api/README.md): `cp .env.example .env`, preencha os segredos e rode `docker compose up --build`. O painel roda com `npm run dev` em `admin/` ([admin/README.md](admin/README.md)).

## Produção

API em https://api.madebyjoaogomes.com, painel em https://admin.madebyjoaogomes.com. Infraestrutura e operação em [deploy/README.md](deploy/README.md).
