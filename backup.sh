#!/usr/bin/env bash
set -euo pipefail
umask 077
cd "$(dirname "$0")"
backup_dir="backups/backup-$(date +%Y%m%d-%H%M%S)-$$"
mkdir -p "$backup_dir"
docker compose exec -T db pg_dump -U tamo -d tamo -Fc > "$backup_dir/database.dump"
if [ -d backend/storage ]; then tar -czf "$backup_dir/uploads.tar.gz" -C backend storage; fi
printf '%s\n' "PostgreSQL dump plus private upload files. Contains private records; keep local and access-controlled." > "$backup_dir/README.txt"
printf 'Backup saved: %s\n' "$backup_dir"
