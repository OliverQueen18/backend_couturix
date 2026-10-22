ALTER TABLE users ADD COLUMN plan_code VARCHAR(20) NOT NULL DEFAULT 'GRATUIT';

UPDATE users u
SET plan_code = 'GOLD'
WHERE EXISTS (
    SELECT 1
    FROM user_roles ur
    JOIN roles r ON r.id = ur.role_id
    WHERE ur.user_id = u.id
      AND r.code = 'SUPER_ADMIN'
);
