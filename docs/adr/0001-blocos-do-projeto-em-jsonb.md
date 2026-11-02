# ADR 0001: Blocos de apresentação do projeto em JSONB

- Status: aceito
- Data: 2026-11-02

## Contexto

Um projeto do portfólio tem poucos campos que a API consulta (slug, status, ordem) e três blocos grandes e aninhados que só existem para a página do case: `summary`, `details` (listas de dores, features, imagens, resultados e um depoimento opcional) e `seo`. Normalizar `details` exigiria cinco ou seis tabelas filhas, que seriam sempre lidas e gravadas juntas e nunca filtradas.

## Decisão

- Colunas relacionais para o que é consultado, ordenado ou único: `slug` (unique + check de formato), `status` (check), `display_order`, `title`, `category`, `thumbnail`, `logo` e os timestamps de auditoria.
- `summary`, `details` e `seo` em colunas `jsonb`, mapeadas pelo Hibernate 6 (`@JdbcTypeCode(SqlTypes.JSON)`) para records Java tipados (`ProjectDetails`, `CaseFeature`...).
- A integridade desses blocos fica na Bean Validation dos records (`@NotBlank`, `@Valid` aninhado), que a API aplica em toda escrita. O banco garante só que o JSON existe.
- Artigos não usam JSONB: o frontmatter é plano, então cada campo é uma coluna e as tags ficam em `text[]`.

## Consequências

- O formato de `details` pode evoluir sem migração; um campo novo obrigatório exige tratar os registros antigos (default no record ou migração com `jsonb_set`).
- O JSON gravado tem o mesmo formato que o site já usa em `content/projects/*.json`, o que simplifica importar o conteúdo atual.
- Consultas dentro dos blocos continuam possíveis (`details -> 'features'`), mas não são indexadas; se algum dia forem necessárias, um índice GIN resolve.
