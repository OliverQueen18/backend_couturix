CREATE TABLE site_slides (
    id         BIGSERIAL PRIMARY KEY,
    kicker     VARCHAR(80),
    title      VARCHAR(180) NOT NULL,
    body       VARCHAR(400),
    image_url  VARCHAR(500) NOT NULL,
    cta_label  VARCHAR(80),
    cta_path   VARCHAR(180),
    sort_order INT          NOT NULL DEFAULT 0,
    visible    BOOLEAN      NOT NULL DEFAULT TRUE
);
CREATE INDEX idx_site_slides_visible ON site_slides (visible, sort_order);

INSERT INTO site_slides (kicker, title, body, image_url, cta_label, cta_path, sort_order, visible) VALUES
    ('Bamako', 'Publiez votre maison de couture',
     'Bazin, bogolan et tenues sur mesure, visibles auprès de nouveaux clients.',
     'images/publications/bazin.svg', 'Créer mon atelier', '/inscription', 10, TRUE),
    ('Créations', 'Robes de soirée et de mariée',
     'Mettez vos pièces en avant et recevez des demandes depuis toute la région.',
     'images/publications/soiree.svg', 'Voir le catalogue', '/catalogue', 20, TRUE),
    ('Afrique de l’Ouest', 'De Sikasso à Mopti',
     'Retouches, broderie et prêt-à-porter : faites découvrir votre savoir-faire.',
     'images/publications/retouche.svg', 'Explorer les ateliers', '/ateliers', 30, TRUE);
