INSERT INTO roles (code, libelle) VALUES
    ('SUPER_ADMIN', 'Super administrateur'),
    ('ADMIN', 'Administrateur'),
    ('GERANT', 'Gérant'),
    ('CAISSIER', 'Caissier'),
    ('RECEPTIONNISTE', 'Réceptionniste'),
    ('TAILLEUR', 'Tailleur'),
    ('MAGASINIER', 'Magasinier'),
    ('COMPTABLE', 'Comptable'),
    ('CLIENT', 'Client');

INSERT INTO parametres (cle, valeur, description) VALUES
    ('stock.allowNegative', 'false', 'Autoriser les sorties de stock négatives'),
    ('atelier.nom', 'CouturiX', 'Nom de l''atelier'),
    ('atelier.devise', 'XOF', 'Devise par défaut');

INSERT INTO specialites (libelle) VALUES
    ('Broderie'),
    ('Couture homme'),
    ('Couture femme'),
    ('Couture enfant'),
    ('Retouches');

INSERT INTO categories_modeles (nom, slug, description) VALUES
    ('Boubou', 'boubou', 'Boubous traditionnels et modernes'),
    ('Costume', 'costume', 'Costumes et tailleurs'),
    ('Robe', 'robe', 'Robes de cérémonie et quotidienne'),
    ('Enfant', 'enfant', 'Confections enfant');

INSERT INTO categories_stock (nom) VALUES
    ('Tissus'),
    ('Fils'),
    ('Boutons'),
    ('Accessoires'),
    ('Emballage');

INSERT INTO notification_templates (evenement, canal, sujet, corps) VALUES
    ('COMMANDE_CREEE', 'EMAIL', 'Votre commande {{numero}} a été créée', 'Bonjour {{client}}, votre commande {{numero}} a bien été enregistrée.'),
    ('COMMANDE_CREEE', 'WHATSAPP', NULL, 'CouturiX: commande {{numero}} créée. Merci {{client}}.'),
    ('COMMANDE_CONFIRMEE', 'EMAIL', 'Commande {{numero}} confirmée', 'Bonjour {{client}}, votre commande {{numero}} est confirmée.'),
    ('COMMANDE_CONFIRMEE', 'WHATSAPP', NULL, 'CouturiX: commande {{numero}} confirmée.'),
    ('STATUT_CHANGE', 'EMAIL', 'Mise à jour de la commande {{numero}}', 'Votre commande {{numero}} est maintenant : {{statut}}.'),
    ('STATUT_CHANGE', 'WHATSAPP', NULL, 'CouturiX: commande {{numero}} → {{statut}}.'),
    ('COMMANDE_PRETE', 'EMAIL', 'Votre commande {{numero}} est prête', 'Bonjour {{client}}, votre commande {{numero}} est prête.'),
    ('COMMANDE_PRETE', 'WHATSAPP', NULL, 'CouturiX: commande {{numero}} prête.'),
    ('PAIEMENT', 'EMAIL', 'Paiement reçu', 'Nous avons reçu un paiement de {{montant}} pour la commande {{numero}}.'),
    ('PAIEMENT', 'WHATSAPP', NULL, 'CouturiX: paiement {{montant}} reçu ({{numero}}).'),
    ('RAPPEL', 'EMAIL', 'Rappel CouturiX', '{{message}}'),
    ('RAPPEL', 'WHATSAPP', NULL, 'CouturiX: {{message}}'),
    ('LIVRAISON', 'EMAIL', 'Commande {{numero}} livrée', 'Votre commande {{numero}} a été livrée. Merci.'),
    ('LIVRAISON', 'WHATSAPP', NULL, 'CouturiX: commande {{numero}} livrée. Merci.');
