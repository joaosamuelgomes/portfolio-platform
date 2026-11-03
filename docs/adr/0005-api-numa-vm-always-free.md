# ADR 0005: API e banco numa VM Always Free da Oracle Cloud

- Status: aceito
- Data: 2026-11-02

## Contexto

A API (Spring Boot) e o PostgreSQL precisam de um endereço público e estável para o painel e para o build do site. O projeto é uma vitrine pessoal: o custo precisa ficar em zero e o tráfego é baixo. O site e o painel são estáticos e já podem ficar na Vercel.

## Decisão

Uma VM `VM.Standard.A1.Flex` (Ampere, ARM) da Oracle Cloud, dentro do Always Free (2 OCPUs e 12 GB de 4 e 24 disponíveis), roda tudo com Docker Compose: PostgreSQL, a API e o Caddy na frente, com HTTPS automático em `api.madebyjoaogomes.com`.

- **Sem portas internas expostas:** só o Caddy publica 80/443; banco e API ficam na rede do Compose.
- **Segredos gerados na VM:** o `.env` de produção é criado na própria máquina (`chmod 600`) e não passa pelo repositório nem pela Vercel. A URL do Deploy Hook também fica só lá.
- **Deploy pela CI:** cada push na `main` testa, publica a imagem ARM no GHCR e faz o deploy por SSH com uma chave dedicada; o token do GitHub só passa pela VM durante o `docker pull`. O `deploy/push.sh` (snapshot do commit por SSH, imagem construída na VM) fica como caminho manual.
- **Custo vigiado:** um budget de R$ 1/mês na conta, com alerta em 80%.
- **Backup:** `pg_dump` diário no disco da VM, com 14 dias de retenção, e uma cópia num bucket privado do Object Storage (Always Free) por uma URL pré-autenticada só de escrita.

## Alternativas

- **PaaS gratuito (Render, Railway, Fly):** menos operação, mas os planos grátis dormem (cold start de dezenas de segundos), expiram o banco ou cobram depois de um limite.
- **Banco gerenciado (Neon, Supabase) com a API num PaaS:** mais peças e contas para uma vitrine.
- **AMD `E2.1.Micro`:** também Always Free, mas 1 GB de RAM é pouco para a JVM e o PostgreSQL juntos.

## Consequências

- Operação por conta própria: atualizações do Ubuntu, do Docker e das imagens são manuais.
- Ponto único de falha. Se a VM cair, o painel para; o site, estático, continua no ar com o último build.
- A cópia no Object Storage cobre a perda da VM. Como a URL só escreve, uma VM comprometida não apaga o histórico; a retenção fica a cargo de uma regra de ciclo de vida do bucket.
- A imagem ARM é construída na CI com o estágio de build nativo (só o runtime passa por emulação) e fica no GHCR, privada como o repositório.
