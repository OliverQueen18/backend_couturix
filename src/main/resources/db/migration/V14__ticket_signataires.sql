CREATE TABLE ticket_signataires (
    id         BIGSERIAL PRIMARY KEY,
    atelier_id BIGINT         NOT NULL REFERENCES ateliers (id) ON DELETE CASCADE,
    libelle    VARCHAR(80)    NOT NULL,
    nom        VARCHAR(120),
    ordre      INTEGER        NOT NULL DEFAULT 0,
    actif      BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_ticket_signataires_atelier ON ticket_signataires (atelier_id, ordre);
