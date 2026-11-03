# Deploy

Produção da API: uma VM Always Free da Oracle Cloud com Docker Compose (PostgreSQL, API e Caddy). O painel e o site ficam na Vercel. Decisão e alternativas no [ADR 0005](../docs/adr/0005-api-numa-vm-always-free.md).

```
madebyjoaogomes.com ─── Vercel (site estático) ── build lê ──┐
admin.madebyjoaogomes.com ─ Vercel (painel) ── HTTPS ──┐    │
                                                       ▼    ▼
api.madebyjoaogomes.com ── Oracle Cloud VM: Caddy :443 → API :8080 → PostgreSQL
```

| Peça | Onde | Detalhes |
| --- | --- | --- |
| VM | Oracle Cloud, `sa-saopaulo-1` | `VM.Standard.A1.Flex` (Ampere, ARM), 2 OCPUs, 12 GB, Ubuntu 24.04, disco de 47 GB. Tudo dentro do Always Free (até 4 OCPUs, 24 GB e 200 GB de disco) |
| Rede | VCN `portfolio-vcn`, sub-rede `portfolio-public` | Entrada liberada só em 22, 80 e 443 (security list e iptables da VM) |
| Custo | Budget `budget-vps` | R$ 1/mês na conta, com alerta em 80% do gasto real |
| DNS | Hostinger | `A api → IP da VM`; `CNAME admin → Vercel` |
| Site | Vercel, projeto `portfolio` | `CONTENT_API_URL=https://api.madebyjoaogomes.com`; Deploy Hook `painel-admin` |
| Painel | Vercel, projeto `portfolio-admin` | Raiz `admin/`, `VITE_API_URL=https://api.madebyjoaogomes.com` |

## Arquivos

- `compose.yml`: a stack. Só o Caddy publica portas; banco e API ficam na rede interna.
- `Caddyfile`: HTTPS automático (Let's Encrypt) para `API_DOMAIN`, corpo das requisições limitado a 1 MB e proxy para a API.
- `setup.sh`: prepara uma VM Ubuntu nova (Docker, iptables, rpcbind desligado, swap e o backup no cron).
- `backup.sh`: `pg_dump` diário às 03:30 em `deploy/backups/` (só o dono lê), mantendo os 14 mais recentes.
- `push.sh`: envia `api/` e `deploy/` de um commit para a VM e reconstrói a stack.
- `.env` (só na VM, `chmod 600`): segredos de produção, gerados na própria VM.

## Atualizar a API

O repositório é privado e a VM não tem credencial do GitHub: ela recebe um snapshot do commit por SSH.

```bash
DEPLOY_HOST=ubuntu@163.176.137.21 deploy/push.sh origin/main
```

## Operação

```bash
ssh -i ~/.ssh/portfolio_oci ubuntu@163.176.137.21
```

```bash
cd ~/portfolio-platform/deploy && docker compose ps && docker compose logs -f api
```

- **Senhas do painel:** `grep -E '^(ADMIN|VIEWER)_' ~/portfolio-platform/deploy/.env` na VM. Para trocar uma senha, edite o `.env` e rode `docker compose up -d api`.
- **Restaurar um backup:** `gunzip -c backups/<arquivo>.sql.gz | docker compose exec -T db psql -U portfolio portfolio`.
- **Recriar do zero:** VM Ubuntu 24.04 com as portas liberadas, copiar `api/` e `deploy/`, rodar `deploy/setup.sh`, criar o `.env` (variáveis em `compose.yml`) e `docker compose up -d --build`. Depois, `scripts/import-content.mjs --api https://api.madebyjoaogomes.com` num banco vazio.

## Limites conhecidos

- Uma VM só, sem réplica. Se ela cair, o painel para, mas o site continua no ar (é estático) e só não recebe atualizações.
- O backup fica no mesmo disco da VM. Protege contra erro de operação, não contra perda da VM.
- O IP público é efêmero: sobrevive a reinicializações, mas muda se a instância for terminada (aí é preciso atualizar o registro `api` no DNS).
