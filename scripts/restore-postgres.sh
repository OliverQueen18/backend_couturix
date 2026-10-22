#!/usr/bin/env bash
set -euo pipefail
FILE="${1:?Usage: restore-postgres.sh backup/fichier.sql}"
docker compose exec -T postgres psql -U "${POSTGRES_USER:-couturix}" "${POSTGRES_DB:-couturix}" < "$FILE"
echo "Restauration terminée : $FILE"
