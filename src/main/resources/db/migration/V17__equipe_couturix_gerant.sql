UPDATE roles SET libelle = 'Administrateur CouturiX' WHERE code = 'ADMIN';
UPDATE roles SET libelle = 'Administrateur d’atelier' WHERE code = 'GERANT';

INSERT INTO user_roles (user_id, role_id)
SELECT ur.user_id, (SELECT id FROM roles WHERE code = 'GERANT')
FROM user_roles ur
JOIN users u ON u.id = ur.user_id
JOIN roles r ON r.id = ur.role_id
WHERE r.code = 'ADMIN'
  AND u.atelier_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM user_roles ur2
      JOIN roles r2 ON r2.id = ur2.role_id
      WHERE ur2.user_id = ur.user_id
        AND r2.code IN ('SUPER_ADMIN', 'GERANT')
  )
ON CONFLICT DO NOTHING;

DELETE FROM user_roles ur
USING users u, roles r
WHERE ur.user_id = u.id
  AND ur.role_id = r.id
  AND r.code = 'ADMIN'
  AND u.atelier_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM user_roles ur2
      JOIN roles r2 ON r2.id = ur2.role_id
      WHERE ur2.user_id = u.id
        AND r2.code = 'SUPER_ADMIN'
  );
