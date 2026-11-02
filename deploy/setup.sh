#!/usr/bin/env bash
# One-time preparation of a fresh Ubuntu 24.04 VM (Oracle Cloud, ARM or x86): Docker,
# firewall openings for HTTP/HTTPS, a swap file and the daily backup. Safe to run again.
set -euo pipefail

if ! command -v docker >/dev/null; then
    curl -fsSL https://get.docker.com | sudo sh
    sudo usermod -aG docker "$USER"
fi

# Oracle's Ubuntu images ship iptables rules that reject everything but SSH; the VCN
# security list must also allow 80 and 443.
for port in 80 443; do
    sudo iptables -C INPUT -p tcp --dport "$port" -j ACCEPT 2>/dev/null \
        || sudo iptables -I INPUT 5 -p tcp --dport "$port" -j ACCEPT
done
sudo iptables -C INPUT -p udp --dport 443 -j ACCEPT 2>/dev/null || sudo iptables -I INPUT 5 -p udp --dport 443 -j ACCEPT
sudo sh -c 'iptables-save > /etc/iptables/rules.v4'

if ! swapon --show | grep -q /swapfile; then
    sudo fallocate -l 2G /swapfile
    sudo chmod 600 /swapfile
    sudo mkswap /swapfile
    sudo swapon /swapfile
    echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab >/dev/null
fi

deploy_dir="$(cd "$(dirname "$0")" && pwd)"
chmod +x "$deploy_dir/backup.sh"
( crontab -l 2>/dev/null | grep -v portfolio-backup; echo "30 3 * * * $deploy_dir/backup.sh >> $deploy_dir/backups.log 2>&1 # portfolio-backup" ) | crontab -

echo "Pronto. Saia e entre de novo no SSH para usar o docker sem sudo."
