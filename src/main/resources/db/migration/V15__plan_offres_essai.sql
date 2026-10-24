CREATE TABLE plan_offres (
    code         VARCHAR(20) PRIMARY KEY,
    libelle      VARCHAR(80)  NOT NULL,
    trial_days   INTEGER      NOT NULL DEFAULT 0,
    max_ateliers INTEGER,
    sms          BOOLEAN      NOT NULL DEFAULT FALSE,
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

INSERT INTO plan_offres (code, libelle, trial_days, max_ateliers, sms) VALUES
    ('GRATUIT',  'Gratuit',  15, 1,    FALSE),
    ('STANDARD', 'Standard', 0,  1,    TRUE),
    ('PREMIUM',  'Premium',  0,  5,    TRUE),
    ('GOLD',     'Gold',     0,  NULL, TRUE);

ALTER TABLE users ADD COLUMN plan_started_at TIMESTAMPTZ;
UPDATE users SET plan_started_at = COALESCE(created_at, NOW());
ALTER TABLE users ALTER COLUMN plan_started_at SET DEFAULT NOW();
ALTER TABLE users ALTER COLUMN plan_started_at SET NOT NULL;
