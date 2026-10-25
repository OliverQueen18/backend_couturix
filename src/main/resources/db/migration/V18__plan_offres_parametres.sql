ALTER TABLE plan_offres
    ADD COLUMN prix_mensuel INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN remise_annuelle INTEGER NOT NULL DEFAULT 20,
    ADD COLUMN kicker VARCHAR(80),
    ADD COLUMN description TEXT,
    ADD COLUMN cta VARCHAR(80),
    ADD COLUMN popular BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN actif BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN features JSONB NOT NULL DEFAULT '[]'::jsonb,
    ADD COLUMN services JSONB NOT NULL DEFAULT '[]'::jsonb;

UPDATE plan_offres SET prix_mensuel = 0, kicker = '15 jours d’essai', cta = 'Commencer gratuitement' WHERE code = 'GRATUIT';
UPDATE plan_offres SET prix_mensuel = 15000, kicker = 'Présence', cta = 'Choisir Standard' WHERE code = 'STANDARD';
UPDATE plan_offres SET prix_mensuel = 35000, kicker = 'Le plus choisi', cta = 'Choisir Premium', popular = TRUE WHERE code = 'PREMIUM';
UPDATE plan_offres SET prix_mensuel = 60000, kicker = 'Maison', cta = 'Choisir Gold' WHERE code = 'GOLD';
