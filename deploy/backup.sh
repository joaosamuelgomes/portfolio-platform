#!/usr/bin/env sh
# Daily logical backup of the database, keeping the last 14 dumps. Installed as a cron job
# by setup.sh; restore with: gunzip -c <file> | docker compose exec -T db psql -U portfolio portfolio
set -eu

cd "$(dirname "$0")"
mkdir -p backups
file="backups/portfolio-$(date +%Y%m%d-%H%M%S).sql.gz"
docker compose exec -T db pg_dump -U portfolio --clean --if-exists portfolio | gzip > "$file"
ls -1t backups/portfolio-*.sql.gz | tail -n +15 | xargs -r rm --
echo "backup: $file"
