# portfolio-api

API de conteúdo do portfólio [madebyjoaogomes.com](https://madebyjoaogomes.com): o painel admin cadastra e publica projetos e artigos, e o build do site estático lê o que está publicado em `GET /public/content`.

Java 21 · Spring Boot 3.5 · PostgreSQL 16 + Flyway · Spring Security (JWT) · springdoc-openapi · JUnit 5, Mockito e Testcontainers

## Rodando

Pré-requisitos: Java 21 e Docker. O Maven vem pelo wrapper (`./mvnw`).

### Tudo em containers

Na raiz do repositório:

```bash
cp .env.example .env
```

Preencha `JWT_SECRET` (32+ caracteres, por exemplo `openssl rand -base64 48`) e as senhas das contas em `.env`, depois:

```bash
docker compose up --build
```

A API sobe em http://localhost:8080 e o Swagger UI em http://localhost:8080/swagger-ui/index.html.

### API local, banco em container

```bash
docker compose up -d db
```

```bash
cd api
JWT_SECRET=<32+ caracteres> ADMIN_PASSWORD=<senha> VIEWER_PASSWORD=<senha> LOG_FORMAT= ./mvnw spring-boot:run
```

`LOG_FORMAT=` vazio troca os logs JSON (ECS) pelo formato legível de desenvolvimento.

### Importando o conteúdo do site

Antes de o build do site ler a API, o banco precisa ter o conteúdo que hoje está em `content/` no repositório do site. O script `scripts/import-content.mjs` (Node 20.12+) lê `content/projects/*.json` e `content/articles/*.md`, cria tudo pelos endpoints de admin e publica o que tem `published: true`; o resto fica como `DRAFT`. O nome do arquivo vira o `slug`.

Com a API no ar (`docker compose up --build`) e `ADMIN_PASSWORD` no `.env` da raiz:

```bash
cd scripts
npm install
node import-content.mjs --dry-run
```

```bash
node import-content.mjs
```

| Opção | Padrão | Uso |
| --- | --- | --- |
| `--content <pasta>` (ou `CONTENT_DIR`) | `../portfolio/content`, ao lado deste repositório | Pasta `content/` do site |
| `--api <url>` (ou `API_URL`) | `http://localhost:8080` | Onde a API está |
| `--dry-run` | | Só lê e valida os arquivos, sem chamar a API |

O usuário e a senha vêm de `ADMIN_USERNAME` e `ADMIN_PASSWORD` (do ambiente ou do `.env`).

Os ids dos projetos aparecem nas URLs `/case/{id}` e não podem mudar. Como a API gera o id, o script cria os projetos em ordem de id num banco vazio e confere cada id devolvido; por isso:

- os ids em `content/projects` precisam ser `1..n`, sem lacunas;
- o script recusa rodar se o banco já tem qualquer projeto ou artigo. Para importar de novo, recomece o banco com `docker compose down -v`;
- se a sequência do banco já tiver sido usada (projetos criados e excluídos), o primeiro id sai errado: o script desfaz o que criou e para.

Depois da carga, a ordem de exibição segue o campo `order` dos arquivos e o script compara `GET /public/content` com os arquivos: ids, ordem e conteúdo de cada projeto e artigo publicado, e se os rascunhos ficaram como `DRAFT`. Qualquer diferença (por exemplo, um campo que a API não aceita e descartaria) faz o script terminar com erro.

### Testes

```bash
cd api
./mvnw verify
```

`mvn test` roda os testes unitários (`*Test`, serviços com Mockito). `verify` também roda os de integração (`*IT`) com o Failsafe: sobem a aplicação inteira contra um PostgreSQL real via Testcontainers, aplicam as migrações e cobrem os endpoints, a validação, o snapshot público e as regras de autorização. Precisa do Docker rodando.

## Configuração

| Variável | Padrão | Uso |
| --- | --- | --- |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/portfolio` | JDBC do PostgreSQL |
| `DATABASE_USERNAME` / `DATABASE_PASSWORD` | `portfolio` / `portfolio` | Credenciais do banco |
| `JWT_SECRET` | (obrigatória) | Chave HS256 dos tokens, 32+ caracteres |
| `JWT_TTL` | `PT1H` | Validade do token (ISO-8601) |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | `admin` / vazio | Conta que escreve. Sem senha, fica desativada |
| `VIEWER_USERNAME` / `VIEWER_PASSWORD` | `recrutador` / vazio | Conta demo só leitura. Sem senha, fica desativada |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Origens do painel admin, separadas por vírgula |
| `SITE_DEPLOY_HOOK_URL` | vazio | Deploy Hook do Vercel que gera o site de novo ([ADR 0004](../docs/adr/0004-site-le-a-api-no-build.md)). Segredo; sem ela, "Atualizar site" fica desativado |
| `LOG_FORMAT` | `ecs` | Formato dos logs estruturados (`ecs`, `logstash`, `gelf`); vazio desliga |

As senhas podem ser texto (viram BCrypt na inicialização) ou um hash pronto no formato `{bcrypt}$2a$...`.

## Endpoints

| Método e rota | Acesso | O que faz |
| --- | --- | --- |
| `POST /auth/token` | aberto | Troca usuário e senha por um JWT |
| `GET /public/content` | aberto | Projetos e artigos publicados, no formato do snapshot do site |
| `GET /admin/projects`, `GET /admin/projects/{id}` | ADMIN, VIEWER | Lista (filtro opcional `?status=`) e detalhe |
| `POST`, `PUT /{id}`, `DELETE /{id}` em `/admin/projects` | ADMIN | Cria (como rascunho, no fim da ordem), substitui e exclui |
| `POST /admin/projects/{id}/publish`, `/unpublish`, `/archive` | ADMIN | Muda o status |
| `PUT /admin/projects/order` | ADMIN | Recebe todos os ids na nova ordem |
| `/admin/articles/...` | idem | Mesmo conjunto para artigos, sem a ordenação |
| `GET /admin/site` | ADMIN, VIEWER | Se o deploy do site está configurado e quando foi o último pedido |
| `POST /admin/site/deploy` | ADMIN | Chama o Deploy Hook: o site é gerado de novo com o conteúdo publicado |
| `GET /actuator/health` | aberto | Saúde, com probes de liveness e readiness |
| `/swagger-ui/index.html`, `/v3/api-docs` | aberto | Documentação e contrato OpenAPI |

Exemplo:

```bash
TOKEN=$(curl -s -X POST localhost:8080/auth/token -H 'Content-Type: application/json' -d '{"username":"admin","password":"<senha>"}' | jq -r .accessToken)
```

```bash
curl -H "Authorization: Bearer $TOKEN" localhost:8080/admin/projects
```

Erros seguem a RFC 9457 (`application/problem+json`); erros de validação trazem a lista `errors` com `field` e `message`.

## Modelo

- **Status**: `DRAFT`, `PUBLISHED` ou `ARCHIVED`. Só `PUBLISHED` aparece em `/public/content`.
- **Projeto**: `slug` único, `order` de exibição e o `id` numérico usado nas URLs `/case/{id}` do site. Os blocos `summary`, `details` e `seo` ficam em JSONB ([ADR 0001](../docs/adr/0001-blocos-do-projeto-em-jsonb.md)).
- **Artigo**: frontmatter em colunas e corpo em Markdown cru; o site renderiza o HTML ([ADR 0003](../docs/adr/0003-formato-do-snapshot.md)). Publicar um artigo sem data usa a data de hoje (fuso `America/Sao_Paulo`). O corpo não pode ter `# ` (o título já é o h1 da página).

## Estrutura

```
src/main/java/com/madebyjoaogomes/portfolio/
├── project/    entidade, records do case, serviço e controller de /admin/projects
├── article/    entidade, serviço e controller de /admin/articles
├── content/    status compartilhado e o snapshot público (/public/content)
├── security/   JWT, contas e regras de acesso
├── site/       pedido de deploy do site estático (Deploy Hook)
└── web/        ProblemDetail e configuração do OpenAPI
src/main/resources/db/migration/   migrações Flyway
```

## Decisões

- [ADR 0001: Blocos de apresentação do projeto em JSONB](../docs/adr/0001-blocos-do-projeto-em-jsonb.md)
- [ADR 0002: Autenticação JWT stateless emitida pela própria API](../docs/adr/0002-autenticacao-jwt-stateless.md)
- [ADR 0003: Formato do snapshot público](../docs/adr/0003-formato-do-snapshot.md)
- [ADR 0004: O site lê a API no build e é atualizado por um pedido explícito](../docs/adr/0004-site-le-a-api-no-build.md)
