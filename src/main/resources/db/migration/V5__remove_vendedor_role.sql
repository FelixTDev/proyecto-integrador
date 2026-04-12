SET NAMES utf8mb4;

INSERT INTO rol (nombre)
SELECT 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'ADMIN');

INSERT INTO rol (nombre)
SELECT 'CLIENTE'
WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'CLIENTE');

INSERT INTO usuario_rol (usuario_id, rol_id)
SELECT ur.usuario_id, r_admin.id
FROM usuario_rol ur
JOIN rol r_v ON r_v.id = ur.rol_id AND r_v.nombre = 'VENDEDOR'
JOIN rol r_admin ON r_admin.nombre = 'ADMIN'
LEFT JOIN usuario_rol ur_admin ON ur_admin.usuario_id = ur.usuario_id AND ur_admin.rol_id = r_admin.id
WHERE ur_admin.usuario_id IS NULL;

DELETE ur
FROM usuario_rol ur
JOIN rol r ON r.id = ur.rol_id
WHERE r.nombre = 'VENDEDOR';

DELETE FROM rol
WHERE nombre = 'VENDEDOR';
