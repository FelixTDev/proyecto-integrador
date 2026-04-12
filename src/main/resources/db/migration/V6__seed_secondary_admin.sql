SET NAMES utf8mb4;

INSERT INTO rol (nombre)
SELECT 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'ADMIN');

INSERT INTO usuario (nombre, email, password_hash, telefono, activo, fecha_verificacion_email, intentos_fallidos_login, fecha_actualizacion)
SELECT
    'Administrador Secundario',
    'admin2@casachantilly.pe',
    COALESCE(
        (SELECT u.password_hash FROM usuario u WHERE u.email = 'admin@casachantilly.pe' LIMIT 1),
        '$2a$10$P671tvW64nmVSpr6nGgWruchBD.Kkdj.Axrceg0IU8/89N4cqUZTm'
    ),
    '999200300',
    1,
    NOW(),
    0,
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'admin2@casachantilly.pe');

INSERT INTO usuario_rol (usuario_id, rol_id)
SELECT u.id, r.id
FROM usuario u
JOIN rol r ON r.nombre = 'ADMIN'
WHERE u.email = 'admin2@casachantilly.pe'
  AND NOT EXISTS (
      SELECT 1
      FROM usuario_rol ur
      WHERE ur.usuario_id = u.id
        AND ur.rol_id = r.id
  );
