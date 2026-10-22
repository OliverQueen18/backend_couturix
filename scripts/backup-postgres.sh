#!/usr/bin/env bash
set -euo pipefail
mkdir -p backup
FILE="backup/couturix-$(date +%F-%H%M).sql"
docker compose exec -T postgres pg_dump -U "${POSTGRES_USER:-couturix}" "${POSTGRES_DB:-couturix}" > "$FILE"
echo "Sauvegarde écrite : $FILE"
