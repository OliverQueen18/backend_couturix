# CouturiX — Backend

API REST de **gestion intelligente d’atelier de couture** (Spring Boot 3.5, Java 21, PostgreSQL, JWT).

Tagline : *SIMPLE • EFFICACE • RENTABLE*

## Prérequis

- Java 21
- Maven 3.9+
- PostgreSQL 16 (ou Docker)

## Démarrage local

1. Copier `.env.example` vers `.env` et renseigner `JWT_SECRET` et `ADMIN_PASSWORD`.
2. Lancer PostgreSQL :

```bash
docker compose up -d postgres
```

3. Démarrer l’API :

```bash
./mvnw spring-boot:run
```

- API : http://localhost:8080/api/v1
- Swagger : http://localhost:8080/swagger-ui.html
- Santé : http://localhost:8080/actuator/health

Compte SUPER_ADMIN : e-mail et mot de passe de `ADMIN_EMAIL` / `ADMIN_PASSWORD`
(ou `COUTURIX_ADMIN_EMAIL` / `COUTURIX_ADMIN_PASSWORD` sur le VPS). Ils sont
resynchronisés à chaque démarrage.

## Variables d’environnement

Aucune clé n’est codée en dur. Voir `.env.example` :

| Variable | Rôle |
| --- | --- |
| `DATABASE_URL` / `USERNAME` / `PASSWORD` | PostgreSQL |
| `JWT_SECRET` | Signature des jetons (≥ 32 caractères) |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | Compte SUPER_ADMIN (synchronisé au démarrage) |
| `MAIL_*` | SMTP (Mailhog en local) |
| `WHATSAPP_*` | API WhatsApp Business |
| `SMS_GATEWAY_*` | Envoi SMS (Android SMS Gateway), dès l’offre Standard |
| `STOCK_ALLOW_NEGATIVE` | Sorties de stock négatives |

## Modules

Authentification, utilisateurs/rôles, clients & mesures, modèles, commandes & production, stocks, fournisseurs, caisse, paiements, tailleurs & paie, compositions, moteur de coûts, simulateur, devis, notifications e-mail/WhatsApp/SMS, tableau de bord, rapports, administration.

## Tests

```bash
./mvnw test
```

## Sauvegarde PostgreSQL

```bash
docker compose exec postgres pg_dump -U couturix couturix > backup/couturix-$(date +%F).sql
```

Restauration :

```bash
docker compose exec -T postgres psql -U couturix couturix < backup/couturix-YYYY-MM-DD.sql
```

## Production (VPS OliveApps, comme Dibidani)

Deux jobs Jenkins (backend / frontend) construisent les images, les poussent sur Docker Hub (`oliverqueen18/couturix-*`), puis le VPS fait `compose pull`.

1. Ajouter les variables de `docs/vps.env.example` au `.env` du VPS (`/home/adminubuntu/OliveApps/.env`).
2. DNS : `couturix.com` et `www.couturix.com` vers l’IP du VPS.
3. Jobs Pipeline from SCM, Script Path `Jenkinsfile`, `DEPLOY_MODE=hub`, `DEPLOY_PATH=/home/adminubuntu/OliveApps`.

Identifiants Jenkins déjà utilisés pour Dibidani : `dockerhub-credentials`, `ssh-server-credentials`.

```bash
mkdir -p /home/adminubuntu/OliveApps/uploads/couturix
docker compose -f docker-compose.yml -f docker-compose.couturix.yml up -d \
  postgres-couturix backend-couturix frontend-couturix
```

nginx-proxy termine le TLS. Le Nginx du frontend reverse-proxie `/api/` vers l’API interne.
