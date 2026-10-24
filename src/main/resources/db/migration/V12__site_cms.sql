CREATE TABLE site_settings (
    id               SMALLINT PRIMARY KEY DEFAULT 1 CHECK (id = 1),
    brand_name       VARCHAR(80)  NOT NULL DEFAULT 'CouturiX',
    tagline          VARCHAR(255),
    footer_tagline   VARCHAR(255),
    footer_note      VARCHAR(255),
    header_cta_label VARCHAR(80),
    header_cta_path  VARCHAR(180),
    contact_email    VARCHAR(180),
    contact_phones   VARCHAR(180),
    contact_address  VARCHAR(255),
    facebook_url     VARCHAR(255),
    instagram_url    VARCHAR(255),
    tiktok_url       VARCHAR(255),
    linkedin_url     VARCHAR(255),
    youtube_url      VARCHAR(255),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE site_menus (
    id         BIGSERIAL PRIMARY KEY,
    location   VARCHAR(40)  NOT NULL,
    label      VARCHAR(80)  NOT NULL,
    path       VARCHAR(180) NOT NULL,
    sort_order INT          NOT NULL DEFAULT 0,
    visible    BOOLEAN      NOT NULL DEFAULT TRUE
);
CREATE INDEX idx_site_menus_location ON site_menus (location, sort_order);

CREATE TABLE site_pages (
    id         BIGSERIAL PRIMARY KEY,
    slug       VARCHAR(120) NOT NULL UNIQUE,
    title      VARCHAR(180) NOT NULL,
    eyebrow    VARCHAR(80),
    lead       TEXT,
    body       TEXT         NOT NULL,
    published  BOOLEAN      NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

INSERT INTO site_settings (
    id, brand_name, tagline, footer_tagline, footer_note,
    header_cta_label, header_cta_path,
    contact_email, contact_phones, contact_address,
    facebook_url, instagram_url, tiktok_url, linkedin_url, youtube_url
) VALUES (
    1,
    'CouturiX',
    'La plateforme digitale des ateliers de couture.',
    'La plateforme digitale des ateliers de couture.',
    'Simple · Élégant · Rentable',
    'Créer mon atelier',
    '/inscription',
    'services@olive-services.net',
    '(+223) 76 39 69 22 / (+223) 78 17 27 88',
    'Kalaban-Coro ADEKENE, Kati – Koulikoro – Mali',
    'https://facebook.com',
    'https://instagram.com',
    'https://www.tiktok.com',
    'https://www.linkedin.com',
    'https://youtube.com'
);

INSERT INTO site_menus (location, label, path, sort_order, visible) VALUES
    ('HEADER', 'Accueil', '/', 10, TRUE),
    ('HEADER', 'Ateliers', '/ateliers', 20, TRUE),
    ('HEADER', 'Catalogue', '/catalogue', 30, TRUE),
    ('HEADER', 'Tarifs', '/#tarifs', 40, TRUE),
    ('HEADER', 'À propos', '/a-propos', 50, TRUE),
    ('HEADER', 'Contact', '/contact', 60, TRUE),
    ('FOOTER_PLATEFORME', 'Accueil', '/', 10, TRUE),
    ('FOOTER_PLATEFORME', 'Ateliers', '/ateliers', 20, TRUE),
    ('FOOTER_PLATEFORME', 'Catalogue', '/catalogue', 30, TRUE),
    ('FOOTER_PLATEFORME', 'Tarifs', '/#tarifs', 40, TRUE),
    ('FOOTER_ENTREPRISE', 'À propos', '/a-propos', 10, TRUE),
    ('FOOTER_ENTREPRISE', 'Contact', '/contact', 20, TRUE),
    ('FOOTER_ENTREPRISE', 'Partenaires', '/partenaires', 30, TRUE),
    ('FOOTER_LEGAL', 'Conditions d’utilisation', '/conditions-utilisation', 10, TRUE),
    ('FOOTER_LEGAL', 'Politique de confidentialité', '/politique-confidentialite', 20, TRUE);

INSERT INTO site_pages (slug, title, eyebrow, lead, body, published) VALUES
('a-propos', 'À propos', 'Olive Services',
 'CouturiX est conçu et édité par Olive Services, une entreprise malienne de prestations de services et de commerce général, au service des ateliers de couture en Afrique.',
 $p$<section class="story">
<article>
<h2>Olive Services, éditeur de CouturiX</h2>
<p>Olive Services est une entreprise malienne de prestations de services et de commerce général, active depuis 2020 au Mali et en Afrique. Elle conçoit des solutions numériques pour les organisations, les commerces et les maisons de métier.</p>
<p><strong>CouturiX</strong> est l’une de ces solutions : une plateforme SaaS pour publier un atelier de couture, gérer les clients, les commandes, les mesures, la caisse et l’équipe, avec des tarifs affichés uniquement en FCFA.</p>
<p>Le site d’Olive Services présente l’ensemble des métiers du groupe : <a href="https://olive-services.net" target="_blank" rel="noopener">olive-services.net</a>.</p>
</article>
</section>
<section>
<h2>Domaines d’intervention</h2>
<ul class="chips"><li>Génie logiciel</li><li>Web et mobile</li><li>Data et BI</li><li>Cybersécurité</li><li>Réseaux</li><li>Intelligence artificielle</li><li>Audit</li><li>Formation</li><li>GPS</li><li>E-commerce</li></ul>
</section>
<section>
<h2>Ce que CouturiX apporte aux ateliers</h2>
<div class="cards">
<article><h3>Visibilité</h3><p>Une fiche publique, un catalogue de modèles et une présence auprès des clients en Afrique de l’Ouest.</p></article>
<article><h3>Gestion</h3><p>Commandes, mesures, stocks, caisse, paie des tailleurs et suivi de production dans un même espace.</p></article>
<article><h3>Proximité</h3><p>Un éditeur basé à Kati, qui comprend le quotidien des maisons de couture et facture en FCFA.</p></article>
</div>
</section>$p$, TRUE),
('contact', 'Contact', 'Nous écrire',
 'Une question sur CouturiX, un partenariat ou un espace publicitaire ? L’équipe Olive Services vous répond depuis Kati, au Mali.',
 '<p>Les messages sont traités par l’éditeur de CouturiX, du lundi au vendredi.</p>', TRUE),
('partenaires', 'Partenaires', 'Ensemble',
 'Associez votre marque à CouturiX : tissus, machines, formation, finance ou services aux ateliers.',
 $p$<section>
<h2>Pourquoi s’associer à CouturiX</h2>
<p>La plateforme rassemble des ateliers, des tailleurs et des clients autour de la couture. Un partenaire y gagne une visibilité ciblée, en FCFA, auprès de maisons déjà engagées dans leur digitalisation.</p>
<div class="cards">
<article><h3>Espaces publicitaires</h3><p>Bannière d’accueil, cartes sponsorisées et mises en avant saisonnières pour tissus, machines ou accessoires.</p></article>
<article><h3>Marque associée</h3><p>Une présence durable dans l’écosystème CouturiX, auprès des ateliers Premium et Gold.</p></article>
<article><h3>Olive Services</h3><p>Le groupe édite aussi d’autres solutions métier. Un partenariat peut s’étendre au-delà de CouturiX.</p></article>
</div>
</section>
<section class="cta"><div><h2>Devenir partenaire</h2><p>Décrivez votre offre, votre zone et le format souhaité. Nous revenons vers vous avec les disponibilités.</p></div>
<a class="btn" href="/contact?sujet=partenariat">Écrire à Olive Services</a></section>$p$, TRUE),
('conditions-utilisation', 'Conditions d’utilisation', 'Légal',
 'Les présentes conditions encadrent l’accès à CouturiX, la création de comptes atelier ou client, et l’usage des services en ligne.',
 $p$<article class="legal">
<p class="muted">Dernière mise à jour : 24 octobre 2026. Éditeur : Olive Services, Kalaban-Coro ADEKENE, Kati – Koulikoro – Mali.</p>
<h2>1. Objet</h2>
<p>CouturiX est une plateforme en ligne d’annuaire, de catalogue et de gestion d’atelier de couture. L’accès au site public, l’inscription d’un atelier ou d’un client, et l’usage de l’espace connecté impliquent l’acceptation des présentes conditions.</p>
<h2>2. Éditeur et contact</h2>
<p>Le service est édité par Olive Services. Contact : <a href="mailto:services@olive-services.net">services@olive-services.net</a>, (+223) 76 39 69 22 / (+223) 78 17 27 88. Site de l’éditeur : <a href="https://olive-services.net" target="_blank" rel="noopener">olive-services.net</a>.</p>
<h2>3. Comptes</h2>
<p>Un compte atelier est créé par le gérant, avec un plan (Gratuit, Standard, Premium ou Gold) obligatoire. Un compte client permet de suivre des ateliers et de laisser un avis. Chacun est responsable de la confidentialité de ses identifiants.</p>
<h2>4. Offres et prix</h2>
<p>Les prix sont indiqués uniquement en francs CFA (FCFA). À partir de l’offre Standard, les ateliers peuvent envoyer des SMS via SMS Gateway. Olive Services peut faire évoluer les fonctionnalités d’un plan.</p>
<h2>5. Contenus publiés</h2>
<p>Les ateliers restent responsables des photos, modèles, tarifs et informations qu’ils publient.</p>
<h2>6. Commandes et paiements entre utilisateurs</h2>
<p>CouturiX facilite la gestion interne d’un atelier. Sauf mention contraire, Olive Services n’est pas partie au contrat de confection entre un atelier et un client.</p>
<h2>7. Disponibilité</h2>
<p>Le service est fourni « en l’état ». Des interruptions de maintenance peuvent survenir.</p>
<h2>8. Droit applicable</h2>
<p>Les présentes conditions sont régies par le droit malien.</p>
</article>$p$, TRUE),
('politique-confidentialite', 'Politique de confidentialité', 'Légal',
 'Olive Services explique quelles données CouturiX collecte, pourquoi, et comment les exercer.',
 $p$<article class="legal">
<p class="muted">Dernière mise à jour : 24 octobre 2026. Responsable de traitement : Olive Services.</p>
<h2>1. Données collectées</h2>
<p>Compte : nom, prénom, e-mail, téléphone, mot de passe chiffré, rôle. Atelier : raison sociale, adresse, pays, logo, plan d’abonnement. Client d’atelier : mesures, commandes, paiements, avis.</p>
<h2>2. Finalités</h2>
<ul><li>créer et sécuriser les comptes ;</li><li>publier la fiche d’atelier et le catalogue ;</li><li>gérer commandes, caisse, stocks et paie ;</li><li>envoyer des codes OTP, des rappels et des SMS dès l’offre Standard ;</li><li>répondre aux demandes de contact.</li></ul>
<h2>3. Destinataires</h2>
<p>Les données d’un atelier restent isolées des autres maisons. Les prestataires techniques (hébergement, e-mail, SMS Gateway) n’interviennent que pour opérer le service.</p>
<h2>4. Conservation</h2>
<p>Les comptes actifs sont conservés tant que le service est utilisé.</p>
<h2>5. Vos droits</h2>
<p>Vous pouvez demander l’accès, la rectification ou la suppression de vos données en écrivant à <a href="mailto:services@olive-services.net">services@olive-services.net</a>.</p>
<h2>6. Sécurité</h2>
<p>Mots de passe hachés, accès par jeton, séparation des ateliers et HTTPS.</p>
<h2>7. Contact</h2>
<p>Olive Services — Kalaban-Coro ADEKENE, Kati – Koulikoro – Mali.</p>
</article>$p$, TRUE);

UPDATE users u
SET plan_code = 'PLATEFORME'
FROM user_roles ur
JOIN roles r ON r.id = ur.role_id
WHERE ur.user_id = u.id
  AND r.code = 'SUPER_ADMIN';
