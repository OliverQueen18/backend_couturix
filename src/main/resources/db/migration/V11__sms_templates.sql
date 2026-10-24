INSERT INTO notification_templates (evenement, canal, sujet, corps) VALUES
    ('COMMANDE_CREEE', 'SMS', NULL, 'CouturiX: commande {{numero}} enregistree. Merci {{client}}.'),
    ('COMMANDE_CONFIRMEE', 'SMS', NULL, 'CouturiX: commande {{numero}} confirmee.'),
    ('STATUT_CHANGE', 'SMS', NULL, 'CouturiX: commande {{numero}} -> {{statut}}.'),
    ('COMMANDE_PRETE', 'SMS', NULL, 'CouturiX: commande {{numero}} prete. Vous pouvez passer la chercher.'),
    ('PAIEMENT', 'SMS', NULL, 'CouturiX: paiement {{montant}} FCFA recu ({{numero}}).'),
    ('RAPPEL', 'SMS', NULL, 'CouturiX: {{message}}'),
    ('LIVRAISON', 'SMS', NULL, 'CouturiX: commande {{numero}} livree. Merci.')
ON CONFLICT (evenement, canal) DO NOTHING;
