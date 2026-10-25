CREATE TABLE site_media (
    id            BIGSERIAL PRIMARY KEY,
    original_name VARCHAR(180) NOT NULL,
    url           VARCHAR(500) NOT NULL UNIQUE,
    mime          VARCHAR(80),
    size_bytes    BIGINT       NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_site_media_created ON site_media (created_at DESC);
