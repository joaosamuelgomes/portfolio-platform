#!/usr/bin/env sh
# Daily logical backup of the database, keeping the last 14 dumps. Installed as a cron job
# by setup.sh; restore with: gunzip -c <file> | docker compose exec -T db psql -U portfolio portfolio
#
# With BACKUP_UPLOAD_URL in .env (an Object Storage pre-authenticated request that only allows
# writes), each dump is also copied off the VM. The VM can add backups but not read or delete
# them, so losing or breaking into the VM does not take the copies with it.
set -eu
umask 077  # the dumps hold the whole database

cd "$(dirname "$0")"
mkdir -p backups
file="backups/portfolio-$(date +%Y%m%d-%H%M%S).sql.gz"
docker compose exec -T db pg_dump -U portfolio --clean --if-exists portfolio | gzip > "$file"
ls -1t backups/portfolio-*.sql.gz | tail -n +15 | xargs -r rm --
echo "backup: $file"

# tr: tolerates a .env edited on Windows (CRLF line endings).
upload_url="$(grep -s '^BACKUP_UPLOAD_URL=' .env | cut -d= -f2- | tr -d '\r' || true)"
if [ -n "$upload_url" ]; then
    curl -fsS --retry 3 --max-time 300 -T "$file" "${upload_url%/}/$(basename "$file")"
    echo "uploaded: $(basename "$file")"
fi
