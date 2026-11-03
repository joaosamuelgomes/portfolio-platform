#!/usr/bin/env bash
# Ships the API source and this folder to the VM and rebuilds the stack. The repository is
# private, so the server never clones it: it only receives a snapshot of a commit.
#
# Usage: deploy/push.sh [git-ref]   (default: HEAD; needs DEPLOY_HOST and DEPLOY_KEY)
set -euo pipefail

host="${DEPLOY_HOST:?defina DEPLOY_HOST, ex.: ubuntu@163.176.137.21}"
key="${DEPLOY_KEY:-$HOME/.ssh/portfolio_oci}"
ref="${1:-HEAD}"
remote_dir="portfolio-platform"

cd "$(git rev-parse --show-toplevel)"
echo "Enviando $(git rev-parse --short "$ref") para $host..."
git archive --format=tar "$ref" api deploy \
    | ssh -i "$key" "$host" "mkdir -p $remote_dir && tar -x -C $remote_dir"
ssh -i "$key" "$host" "cd $remote_dir/deploy && chmod +x *.sh && docker compose up -d --build --remove-orphans && docker image prune -f >/dev/null"
echo "Pronto."
