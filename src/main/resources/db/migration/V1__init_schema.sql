CREATE TABLE roles (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL UNIQUE,
    libelle     VARCHAR(120) NOT NULL
);

CREATE TABLE users (
    id                    BIGSERIAL PRIMARY KEY,
    email                 VARCHAR(180) NOT NULL UNIQUE,
    password_hash         VARCHAR(255) NOT NULL,
    nom                   VARCHAR(100) NOT NULL,
    prenom                VARCHAR(100) NOT NULL,
    telephone             VARCHAR(40),
    photo_url             VARCHAR(500),
    actif                 BOOLEAN      NOT NULL DEFAULT TRUE,
    must_change_password  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by            VARCHAR(180),
    updated_by            VARCHAR(180)
);
CREATE INDEX idx_users_nom ON users (nom, prenom);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash  VARCHAR(128) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ  NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);

CREATE TABLE password_reset_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash  VARCHAR(128) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ  NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE parametres (
    cle         VARCHAR(100) PRIMARY KEY,
    valeur      VARCHAR(500) NOT NULL,
    description VARCHAR(255),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE clients (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT REFERENCES users (id) ON DELETE SET NULL,
    nom         VARCHAR(100) NOT NULL,
    prenom      VARCHAR(100) NOT NULL,
    telephone   VARCHAR(40)  NOT NULL,
    email       VARCHAR(180),
    adresse     VARCHAR(255),
    photo_url   VARCHAR(500),
    notes       TEXT,
    solde       NUMERIC(14, 2) NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    created_by  VARCHAR(180),
    updated_by  VARCHAR(180)
);
CREATE INDEX idx_clients_nom ON clients (nom, prenom);
CREATE INDEX idx_clients_telephone ON clients (telephone);

CREATE TABLE client_mesures (
    id                 BIGSERIAL PRIMARY KEY,
    client_id          BIGINT         NOT NULL REFERENCES clients (id) ON DELETE CASCADE,
    tour_cou           NUMERIC(8, 2),
    tour_poitrine      NUMERIC(8, 2),
    tour_taille        NUMERIC(8, 2),
    tour_hanches       NUMERIC(8, 2),
    tour_bras          NUMERIC(8, 2),
    tour_poignet       NUMERIC(8, 2),
    epaule             NUMERIC(8, 2),
    longueur_dos       NUMERIC(8, 2),
    longueur_devant    NUMERIC(8, 2),
    longueur_manche    NUMERIC(8, 2),
    tour_cuisse        NUMERIC(8, 2),
    tour_genou         NUMERIC(8, 2),
    bas_pantalon       NUMERIC(8, 2),
    longueur_pantalon  NUMERIC(8, 2),
    hauteur            NUMERIC(8, 2),
    notes              TEXT,
    date_prise         DATE           NOT NULL DEFAULT CURRENT_DATE,
    courante           BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_client_mesures_client ON client_mesures (client_id, date_prise DESC);

CREATE TABLE specialites (
    id      BIGSERIAL PRIMARY KEY,
    libelle VARCHAR(120) NOT NULL UNIQUE
);

CREATE TABLE tailleurs (
    id                 BIGSERIAL PRIMARY KEY,
    user_id            BIGINT REFERENCES users (id) ON DELETE SET NULL,
    nom                VARCHAR(100) NOT NULL,
    prenom             VARCHAR(100) NOT NULL,
    telephone          VARCHAR(40),
    email              VARCHAR(180),
    mode_remuneration  VARCHAR(30)  NOT NULL DEFAULT 'FIXE',
    salaire_fixe       NUMERIC(14, 2) DEFAULT 0,
    taux_piece         NUMERIC(14, 2) DEFAULT 0,
    pourcentage        NUMERIC(6, 3)  DEFAULT 0,
    actif              BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE TABLE tailleur_specialites (
    tailleur_id   BIGINT NOT NULL REFERENCES tailleurs (id) ON DELETE CASCADE,
    specialite_id BIGINT NOT NULL REFERENCES specialites (id) ON DELETE CASCADE,
    PRIMARY KEY (tailleur_id, specialite_id)
);

CREATE TABLE categories_modeles (
    id          BIGSERIAL PRIMARY KEY,
    nom         VARCHAR(120) NOT NULL UNIQUE,
    slug        VARCHAR(140) NOT NULL UNIQUE,
    description TEXT,
    image_url   VARCHAR(500),
    actif       BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE modeles (
    id                        BIGSERIAL PRIMARY KEY,
    categorie_id              BIGINT REFERENCES categories_modeles (id),
    nom                       VARCHAR(180) NOT NULL,
    description               TEXT,
    prix_indicatif            NUMERIC(14, 2),
    tissu_recommande          VARCHAR(180),
    temps_fabrication_minutes INTEGER,
    statut                    VARCHAR(20)  NOT NULL DEFAULT 'BROUILLON',
    created_at                TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at                TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by                VARCHAR(180),
    updated_by                VARCHAR(180)
);
CREATE INDEX idx_modeles_statut ON modeles (statut);
CREATE INDEX idx_modeles_categorie ON modeles (categorie_id);

CREATE TABLE modele_images (
    id         BIGSERIAL PRIMARY KEY,
    modele_id  BIGINT       NOT NULL REFERENCES modeles (id) ON DELETE CASCADE,
    url        VARCHAR(500) NOT NULL,
    ordre      INTEGER      NOT NULL DEFAULT 0,
    principale BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE favoris (
    id         BIGSERIAL PRIMARY KEY,
    client_id  BIGINT NOT NULL REFERENCES clients (id) ON DELETE CASCADE,
    modele_id  BIGINT NOT NULL REFERENCES modeles (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (client_id, modele_id)
);

CREATE TABLE fournisseurs (
    id         BIGSERIAL PRIMARY KEY,
    nom        VARCHAR(180) NOT NULL,
    telephone  VARCHAR(40),
    email      VARCHAR(180),
    adresse    VARCHAR(255),
    notes      TEXT,
    actif      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE categories_stock (
    id  BIGSERIAL PRIMARY KEY,
    nom VARCHAR(120) NOT NULL UNIQUE
);

CREATE TABLE articles_stock (
    id             BIGSERIAL PRIMARY KEY,
    categorie_id   BIGINT REFERENCES categories_stock (id),
    fournisseur_id BIGINT REFERENCES fournisseurs (id),
    nom            VARCHAR(180)  NOT NULL,
    sku            VARCHAR(60)   NOT NULL UNIQUE,
    unite          VARCHAR(30)   NOT NULL DEFAULT 'UNITE',
    stock_actuel   NUMERIC(14, 3) NOT NULL DEFAULT 0,
    stock_minimum  NUMERIC(14, 3) NOT NULL DEFAULT 0,
    prix_achat     NUMERIC(14, 2) NOT NULL DEFAULT 0,
    prix_vente     NUMERIC(14, 2),
    actif          BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_articles_stock_nom ON articles_stock (nom);

CREATE TABLE mouvements_stock (
    id               BIGSERIAL PRIMARY KEY,
    article_id       BIGINT         NOT NULL REFERENCES articles_stock (id),
    type_mouvement   VARCHAR(40)    NOT NULL,
    quantite         NUMERIC(14, 3) NOT NULL,
    stock_avant      NUMERIC(14, 3) NOT NULL,
    stock_apres      NUMERIC(14, 3) NOT NULL,
    motif            VARCHAR(255),
    reference_metier VARCHAR(80),
    user_id          BIGINT REFERENCES users (id),
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_mouvements_article ON mouvements_stock (article_id, created_at DESC);

CREATE TABLE compositions (
    id          BIGSERIAL PRIMARY KEY,
    modele_id   BIGINT      NOT NULL REFERENCES modeles (id) ON DELETE CASCADE,
    version     INTEGER     NOT NULL,
    libelle     VARCHAR(180),
    active      BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (modele_id, version)
);

CREATE TABLE composition_lignes (
    id            BIGSERIAL PRIMARY KEY,
    composition_id BIGINT        NOT NULL REFERENCES compositions (id) ON DELETE CASCADE,
    type_ligne    VARCHAR(40)    NOT NULL,
    article_id    BIGINT REFERENCES articles_stock (id),
    designation   VARCHAR(180)   NOT NULL,
    quantite      NUMERIC(14, 3) NOT NULL,
    cout_unitaire NUMERIC(14, 2) NOT NULL DEFAULT 0
);

CREATE TABLE commandes (
    id             BIGSERIAL PRIMARY KEY,
    numero         VARCHAR(40)    NOT NULL UNIQUE,
    client_id      BIGINT         NOT NULL REFERENCES clients (id),
    modele_id      BIGINT REFERENCES modeles (id),
    composition_id BIGINT REFERENCES compositions (id),
    devis_id       BIGINT,
    tailleur_id    BIGINT REFERENCES tailleurs (id),
    statut         VARCHAR(30)    NOT NULL DEFAULT 'BROUILLON',
    priorite       VARCHAR(20)    NOT NULL DEFAULT 'NORMALE',
    date_prevue    DATE,
    date_livraison DATE,
    quantite       INTEGER        NOT NULL DEFAULT 1,
    prix_unitaire  NUMERIC(14, 2) NOT NULL DEFAULT 0,
    remise         NUMERIC(14, 2) NOT NULL DEFAULT 0,
    montant_total  NUMERIC(14, 2) NOT NULL DEFAULT 0,
    notes          TEXT,
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    created_by     VARCHAR(180),
    updated_by     VARCHAR(180)
);
CREATE INDEX idx_commandes_statut ON commandes (statut);
CREATE INDEX idx_commandes_client ON commandes (client_id);
CREATE INDEX idx_commandes_date_prevue ON commandes (date_prevue);

CREATE TABLE commande_details (
    id            BIGSERIAL PRIMARY KEY,
    commande_id   BIGINT         NOT NULL REFERENCES commandes (id) ON DELETE CASCADE,
    designation   VARCHAR(255)   NOT NULL,
    quantite      INTEGER        NOT NULL DEFAULT 1,
    prix_unitaire NUMERIC(14, 2) NOT NULL DEFAULT 0
);

CREATE TABLE commande_mesures (
    id                 BIGSERIAL PRIMARY KEY,
    commande_id        BIGINT NOT NULL UNIQUE REFERENCES commandes (id) ON DELETE CASCADE,
    tour_cou           NUMERIC(8, 2),
    tour_poitrine      NUMERIC(8, 2),
    tour_taille        NUMERIC(8, 2),
    tour_hanches       NUMERIC(8, 2),
    tour_bras          NUMERIC(8, 2),
    tour_poignet       NUMERIC(8, 2),
    epaule             NUMERIC(8, 2),
    longueur_dos       NUMERIC(8, 2),
    longueur_devant    NUMERIC(8, 2),
    longueur_manche    NUMERIC(8, 2),
    tour_cuisse        NUMERIC(8, 2),
    tour_genou         NUMERIC(8, 2),
    bas_pantalon       NUMERIC(8, 2),
    longueur_pantalon  NUMERIC(8, 2),
    hauteur            NUMERIC(8, 2),
    notes              TEXT
);

CREATE TABLE commande_statuts_historique (
    id          BIGSERIAL PRIMARY KEY,
    commande_id BIGINT      NOT NULL REFERENCES commandes (id) ON DELETE CASCADE,
    ancien      VARCHAR(30),
    nouveau     VARCHAR(30) NOT NULL,
    commentaire VARCHAR(255),
    user_id     BIGINT REFERENCES users (id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_cmd_statuts_cmd ON commande_statuts_historique (commande_id, created_at);

CREATE TABLE production (
    id          BIGSERIAL PRIMARY KEY,
    commande_id BIGINT      NOT NULL UNIQUE REFERENCES commandes (id),
    tailleur_id BIGINT REFERENCES tailleurs (id),
    date_debut  TIMESTAMPTZ,
    date_fin    TIMESTAMPTZ,
    statut      VARCHAR(30) NOT NULL DEFAULT 'EN_ATTENTE',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE production_etapes (
    id            BIGSERIAL PRIMARY KEY,
    production_id BIGINT      NOT NULL REFERENCES production (id) ON DELETE CASCADE,
    etape         VARCHAR(30) NOT NULL,
    commentaire   VARCHAR(255),
    user_id       BIGINT REFERENCES users (id),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE paiements (
    id               BIGSERIAL PRIMARY KEY,
    commande_id      BIGINT REFERENCES commandes (id),
    client_id        BIGINT         NOT NULL REFERENCES clients (id),
    montant          NUMERIC(14, 2) NOT NULL,
    moyen            VARCHAR(30)    NOT NULL,
    date_paiement    DATE           NOT NULL DEFAULT CURRENT_DATE,
    reference        VARCHAR(80),
    notes            VARCHAR(255),
    user_id          BIGINT REFERENCES users (id),
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_paiements_client ON paiements (client_id);

CREATE TABLE caisse (
    id              BIGSERIAL PRIMARY KEY,
    date_ouverture  TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    date_cloture    TIMESTAMPTZ,
    solde_initial   NUMERIC(14, 2) NOT NULL DEFAULT 0,
    solde_theorique NUMERIC(14, 2),
    solde_reel      NUMERIC(14, 2),
    statut          VARCHAR(20)    NOT NULL DEFAULT 'OUVERTE',
    user_id         BIGINT REFERENCES users (id),
    notes           VARCHAR(255)
);

CREATE TABLE caisse_operations (
    id               BIGSERIAL PRIMARY KEY,
    caisse_id        BIGINT         NOT NULL REFERENCES caisse (id),
    type_operation   VARCHAR(20)    NOT NULL,
    categorie        VARCHAR(40)    NOT NULL,
    montant          NUMERIC(14, 2) NOT NULL,
    moyen_paiement   VARCHAR(30)    NOT NULL,
    motif            VARCHAR(255)   NOT NULL,
    reference_metier VARCHAR(80),
    user_id          BIGINT REFERENCES users (id),
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_caisse_ops_caisse ON caisse_operations (caisse_id, created_at);

CREATE TABLE clotures_caisse (
    id          BIGSERIAL PRIMARY KEY,
    caisse_id   BIGINT         NOT NULL UNIQUE REFERENCES caisse (id),
    ecart       NUMERIC(14, 2) NOT NULL DEFAULT 0,
    commentaires TEXT,
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE TABLE avances (
    id          BIGSERIAL PRIMARY KEY,
    tailleur_id BIGINT         NOT NULL REFERENCES tailleurs (id),
    montant     NUMERIC(14, 2) NOT NULL,
    date_avance DATE           NOT NULL DEFAULT CURRENT_DATE,
    motif       VARCHAR(255),
    statut      VARCHAR(20)    NOT NULL DEFAULT 'EN_COURS',
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE TABLE paies (
    id             BIGSERIAL PRIMARY KEY,
    tailleur_id    BIGINT         NOT NULL REFERENCES tailleurs (id),
    periode_debut  DATE           NOT NULL,
    periode_fin    DATE           NOT NULL,
    montant_brut   NUMERIC(14, 2) NOT NULL DEFAULT 0,
    primes         NUMERIC(14, 2) NOT NULL DEFAULT 0,
    retenues       NUMERIC(14, 2) NOT NULL DEFAULT 0,
    avances        NUMERIC(14, 2) NOT NULL DEFAULT 0,
    montant_net    NUMERIC(14, 2) NOT NULL DEFAULT 0,
    statut         VARCHAR(20)    NOT NULL DEFAULT 'BROUILLON',
    notes          TEXT,
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_paies_tailleur ON paies (tailleur_id, periode_debut);

CREATE TABLE simulations (
    id               BIGSERIAL PRIMARY KEY,
    modele_id        BIGINT REFERENCES modeles (id),
    composition_id   BIGINT REFERENCES compositions (id),
    libelle          VARCHAR(180),
    quantite         INTEGER        NOT NULL DEFAULT 1,
    cout_matiere     NUMERIC(14, 2) NOT NULL DEFAULT 0,
    cout_main_oeuvre NUMERIC(14, 2) NOT NULL DEFAULT 0,
    frais_indirects  NUMERIC(14, 2) NOT NULL DEFAULT 0,
    cout_total       NUMERIC(14, 2) NOT NULL DEFAULT 0,
    prix_revient     NUMERIC(14, 2) NOT NULL DEFAULT 0,
    prix_vente       NUMERIC(14, 2) NOT NULL DEFAULT 0,
    remise           NUMERIC(14, 2) NOT NULL DEFAULT 0,
    benefice         NUMERIC(14, 2) NOT NULL DEFAULT 0,
    marge            NUMERIC(8, 4)  NOT NULL DEFAULT 0,
    payload          TEXT,
    user_id          BIGINT REFERENCES users (id),
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE TABLE devis (
    id             BIGSERIAL PRIMARY KEY,
    numero         VARCHAR(40)    NOT NULL UNIQUE,
    client_id      BIGINT         NOT NULL REFERENCES clients (id),
    modele_id      BIGINT REFERENCES modeles (id),
    composition_id BIGINT REFERENCES compositions (id),
    simulation_id  BIGINT REFERENCES simulations (id),
    quantite       INTEGER        NOT NULL DEFAULT 1,
    prix_unitaire  NUMERIC(14, 2) NOT NULL DEFAULT 0,
    remise         NUMERIC(14, 2) NOT NULL DEFAULT 0,
    montant_total  NUMERIC(14, 2) NOT NULL DEFAULT 0,
    delai_jours    INTEGER,
    conditions     TEXT,
    validite       DATE,
    statut         VARCHAR(20)    NOT NULL DEFAULT 'BROUILLON',
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

ALTER TABLE commandes
    ADD CONSTRAINT fk_commandes_devis FOREIGN KEY (devis_id) REFERENCES devis (id);

CREATE TABLE notifications (
    id           BIGSERIAL PRIMARY KEY,
    canal        VARCHAR(20)  NOT NULL,
    evenement    VARCHAR(40)  NOT NULL,
    destinataire VARCHAR(180) NOT NULL,
    sujet        VARCHAR(255),
    contenu      TEXT         NOT NULL,
    statut       VARCHAR(20)  NOT NULL DEFAULT 'EN_ATTENTE',
    erreur       TEXT,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    sent_at      TIMESTAMPTZ
);
CREATE INDEX idx_notifications_statut ON notifications (statut);

CREATE TABLE notification_templates (
    id        BIGSERIAL PRIMARY KEY,
    evenement VARCHAR(40)  NOT NULL,
    canal     VARCHAR(20)  NOT NULL,
    sujet     VARCHAR(255),
    corps     TEXT         NOT NULL,
    UNIQUE (evenement, canal)
);

CREATE TABLE historique_actions (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT,
    action      VARCHAR(80)  NOT NULL,
    entite      VARCHAR(80)  NOT NULL,
    entite_id   BIGINT,
    details     TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_historique_entite ON historique_actions (entite, entite_id);
CREATE INDEX idx_historique_created ON historique_actions (created_at DESC);
