CREATE TABLE ateliers (
    id          BIGSERIAL PRIMARY KEY,
    parent_id   BIGINT REFERENCES ateliers (id) ON DELETE SET NULL,
    nom         VARCHAR(180) NOT NULL,
    slug        VARCHAR(180) NOT NULL UNIQUE,
    description TEXT,
    adresse     VARCHAR(255),
    ville       VARCHAR(120),
    pays        VARCHAR(80)  NOT NULL DEFAULT 'Congo',
    telephone   VARCHAR(40),
    email       VARCHAR(180),
    site_web    VARCHAR(255),
    rccm        VARCHAR(80),
    nif         VARCHAR(80),
    logo_url    VARCHAR(500),
    devise      VARCHAR(10)  NOT NULL DEFAULT 'XOF',
    actif       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by  VARCHAR(180),
    updated_by  VARCHAR(180)
);
CREATE INDEX idx_ateliers_parent ON ateliers (parent_id);
CREATE INDEX idx_ateliers_nom ON ateliers (nom);

INSERT INTO ateliers (nom, slug, description, pays, devise, actif)
VALUES (
    COALESCE((SELECT valeur FROM parametres WHERE cle = 'atelier.nom'), 'CouturiX'),
    'atelier-principal',
    'Atelier principal de la plateforme CouturiX',
    'Congo',
    COALESCE((SELECT valeur FROM parametres WHERE cle = 'atelier.devise'), 'XOF'),
    TRUE
);

ALTER TABLE users ADD COLUMN atelier_id BIGINT REFERENCES ateliers (id);
UPDATE users SET atelier_id = (SELECT id FROM ateliers WHERE slug = 'atelier-principal');
CREATE INDEX idx_users_atelier ON users (atelier_id);

ALTER TABLE clients ADD COLUMN atelier_id BIGINT REFERENCES ateliers (id);
UPDATE clients SET atelier_id = (SELECT id FROM ateliers WHERE slug = 'atelier-principal');
CREATE INDEX idx_clients_atelier ON clients (atelier_id);

ALTER TABLE modeles ADD COLUMN atelier_id BIGINT REFERENCES ateliers (id);
UPDATE modeles SET atelier_id = (SELECT id FROM ateliers WHERE slug = 'atelier-principal');
CREATE INDEX idx_modeles_atelier ON modeles (atelier_id);

ALTER TABLE commandes ADD COLUMN atelier_id BIGINT REFERENCES ateliers (id);
UPDATE commandes SET atelier_id = (SELECT id FROM ateliers WHERE slug = 'atelier-principal');
CREATE INDEX idx_commandes_atelier ON commandes (atelier_id);

ALTER TABLE tailleurs ADD COLUMN atelier_id BIGINT REFERENCES ateliers (id);
UPDATE tailleurs SET atelier_id = (SELECT id FROM ateliers WHERE slug = 'atelier-principal');
CREATE INDEX idx_tailleurs_atelier ON tailleurs (atelier_id);

ALTER TABLE fournisseurs ADD COLUMN atelier_id BIGINT REFERENCES ateliers (id);
UPDATE fournisseurs SET atelier_id = (SELECT id FROM ateliers WHERE slug = 'atelier-principal');
CREATE INDEX idx_fournisseurs_atelier ON fournisseurs (atelier_id);

ALTER TABLE articles_stock ADD COLUMN atelier_id BIGINT REFERENCES ateliers (id);
UPDATE articles_stock SET atelier_id = (SELECT id FROM ateliers WHERE slug = 'atelier-principal');
CREATE INDEX idx_articles_stock_atelier ON articles_stock (atelier_id);

ALTER TABLE caisse ADD COLUMN atelier_id BIGINT REFERENCES ateliers (id);
UPDATE caisse SET atelier_id = (SELECT id FROM ateliers WHERE slug = 'atelier-principal');
CREATE INDEX idx_caisse_atelier ON caisse (atelier_id);

ALTER TABLE devis ADD COLUMN atelier_id BIGINT REFERENCES ateliers (id);
UPDATE devis SET atelier_id = (SELECT id FROM ateliers WHERE slug = 'atelier-principal');
CREATE INDEX idx_devis_atelier ON devis (atelier_id);

ALTER TABLE simulations ADD COLUMN atelier_id BIGINT REFERENCES ateliers (id);
UPDATE simulations SET atelier_id = (SELECT id FROM ateliers WHERE slug = 'atelier-principal');
CREATE INDEX idx_simulations_atelier ON simulations (atelier_id);

ALTER TABLE paiements ADD COLUMN atelier_id BIGINT REFERENCES ateliers (id);
UPDATE paiements SET atelier_id = (SELECT id FROM ateliers WHERE slug = 'atelier-principal');
CREATE INDEX idx_paiements_atelier ON paiements (atelier_id);
