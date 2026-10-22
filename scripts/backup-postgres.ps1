param(
    [string]$OutputDir = "backup"
)
New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null
$stamp = Get-Date -Format "yyyy-MM-dd-HHmm"
$file = Join-Path $OutputDir "couturix-$stamp.sql"
docker compose exec -T postgres pg_dump -U couturix couturix | Out-File -Encoding utf8 $file
Write-Host "Sauvegarde écrite : $file"
