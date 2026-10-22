CREATE TABLE atelier_avis (
    id           BIGSERIAL PRIMARY KEY,
    atelier_id   BIGINT       NOT NULL REFERENCES ateliers (id) ON DELETE CASCADE,
    client_id    BIGINT       NOT NULL REFERENCES clients (id) ON DELETE CASCADE,
    note         SMALLINT     NOT NULL CHECK (note BETWEEN 1 AND 5),
    commentaire  TEXT,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (atelier_id, client_id)
);
CREATE INDEX idx_atelier_avis_atelier ON atelier_avis (atelier_id);
CREATE INDEX idx_atelier_avis_client ON atelier_avis (client_id);
