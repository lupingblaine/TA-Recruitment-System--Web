#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
backup_dir="${1:?Usage: ./verify-backup.sh backups/backup-directory}"
[ -s "$backup_dir/database.dump" ] || { printf '%s\n' 'Missing database.dump'; exit 1; }
restore_db="restore_$(date +%s)_$$_test"
restore_files="$(mktemp -d "${TMPDIR:-/tmp}/tamo-restore.XXXXXX")"
cleanup() { docker compose exec -T db dropdb -U tamo --if-exists "$restore_db" >/dev/null; rm -rf "$restore_files"; }
trap cleanup EXIT
docker compose exec -T db createdb -U tamo "$restore_db"
docker compose exec -T db pg_restore -U tamo -d "$restore_db" --exit-on-error --no-owner --no-privileges < "$backup_dir/database.dump"
docker compose exec -T db psql -U tamo -d "$restore_db" -v ON_ERROR_STOP=1 -c 'SELECT max(version) AS restored_schema_version FROM tamo_schema_version; SELECT count(*) AS restored_applications FROM applications; SELECT count(*) AS restored_assignments FROM section_assignments;'
if [ -f "$backup_dir/uploads.tar.gz" ]; then tar -xzf "$backup_dir/uploads.tar.gz" -C "$restore_files"; [ -d "$restore_files/storage" ]; fi
printf '%s\n' 'PASS: database restored to an isolated test database; upload archive extracted. Production database was not replaced.'
