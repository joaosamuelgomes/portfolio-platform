# ADR 0002: Autenticação JWT stateless emitida pela própria API

- Status: aceito
- Data: 2026-11-02

## Contexto

A API tem dois tipos de cliente autenticado: o painel admin (Fase 3), que escreve conteúdo, e uma conta demo só de leitura para recrutadores explorarem o painel. Há uma pessoa editando, nenhum cadastro de usuários e o servidor roda numa VM pequena do free tier. Um provedor de identidade externo (Keycloak, Auth0) seria mais infraestrutura do que o problema pede.

## Decisão

- `POST /auth/token` troca usuário e senha por um JWT HS256 assinado pela própria API (`NimbusJwtEncoder`), com `iss`, `sub`, `exp` e o claim `roles`. Validade padrão de 1 hora (`JWT_TTL`).
- As demais rotas usam o resource server do Spring Security: o token é validado (assinatura, emissor, expiração) e `roles` vira `ROLE_ADMIN` / `ROLE_VIEWER`. Sem sessão e sem CSRF, já que não há cookie.
- Autorização por rota: `GET /admin/**` para ADMIN e VIEWER; escrita em `/admin/**` só ADMIN; `/public/**`, `/actuator/health` e a documentação OpenAPI abertos; qualquer outra rota negada.
- As contas vêm de variáveis de ambiente (`ADMIN_PASSWORD`, `VIEWER_PASSWORD`...). Senhas em texto são convertidas em BCrypt na inicialização; também é aceito um hash pronto (`{bcrypt}...`). Conta sem senha fica desativada. Nenhuma senha ou segredo real fica no repositório.
- Erros 401/403 seguem o mesmo formato `ProblemDetail` do resto da API e mantêm o cabeçalho `WWW-Authenticate: Bearer`.

## Consequências

- Não há revogação de token: trocar `JWT_SECRET` invalida todos, e a validade curta limita o estrago de um token vazado.
- Não há refresh token; o painel pede um token novo quando o atual expira.
- Trocar para um provedor externo depois é barato: basta apontar o resource server para o JWKS dele e remover o `/auth/token`.
- O login ainda não tem limite de tentativas; antes de expor a API publicamente vale um rate limit no Caddy ou na aplicação.
