# ADR 0004: O site lê a API no build e é atualizado por um pedido explícito

- Status: aceito
- Data: 2026-11-02
- Complementa: [ADR 0003](0003-formato-do-snapshot.md)

## Contexto

O site estático gera, no build, um snapshot do conteúdo. Até aqui a fonte era a pasta `content/` do repositório do site. Com a API e o painel prontos, o conteúdo passa a ser editado no banco, e o site precisa (1) ler a API no build e (2) ser gerado de novo quando algo for publicado. O ADR 0003 previa que, com a API fora do ar, o build usaria "o último snapshot gerado".

## Decisão

**Fonte.** O `scripts/build-content.mjs` do site lê `GET /public/content` quando `CONTENT_API_URL` está definida (produção no Vercel) e a pasta `content/` quando não está (desenvolvimento, CI e testes). As duas fontes passam pelas mesmas checagens e geram o mesmo snapshot; um teste garante isso.

**API fora do ar.** O build tenta três vezes, com timeout de 30 s por tentativa (o host da API pode estar acordando), e então falha. Não há fallback para `content/` nem para um snapshot guardado:

- `src/generated/` não é versionado e o Vercel começa cada build de um clone limpo, então "o último snapshot" não existe lá;
- cair para `content/` publicaria em silêncio um conteúdo que deixou de ser a fonte da verdade;
- um build que falha no Vercel não substitui o deploy em produção, então o site continua no ar com o conteúdo anterior. É o comportamento que o fallback queria garantir, sem conteúdo velho.

**Atualização.** O painel tem um botão "Atualizar site" que chama `POST /admin/site/deploy`; a API faz um POST no Deploy Hook do Vercel (`SITE_DEPLOY_HOOK_URL`). O deploy não acontece a cada publicação: um lote de edições vira um build só, e nada sai pela metade. `GET /admin/site` diz se o hook está configurado e quando foi o último pedido (em memória, desde que a API subiu).

## Consequências

- Publicar no painel muda `/public/content` na hora, mas o site só muda depois de "Atualizar site" e do build (alguns minutos).
- A URL do Deploy Hook é um segredo: fica só na API, nunca no painel.
- O site continua estático: depois do build não depende da API.
- `content/` segue útil para desenvolvimento, CI e como origem do import (`scripts/import-content.mjs`), mas alterações feitas só nela não chegam à produção.
