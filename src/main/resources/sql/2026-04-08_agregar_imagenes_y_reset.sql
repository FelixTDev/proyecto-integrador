ALTER TABLE producto ADD COLUMN imagen_url VARCHAR(500);
ALTER TABLE usuario ADD COLUMN reset_token VARCHAR(36);
ALTER TABLE usuario ADD COLUMN reset_token_expiration DATETIME;

-- Poblar imágenes de los productos existentes en la base de datos local
UPDATE producto SET imagen_url = 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80' WHERE slug = 'selva-negra-clasica';
UPDATE producto SET imagen_url = 'https://images.unsplash.com/photo-1533134242443-d4fd215305ad?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80' WHERE slug = 'cheesecake-maracuya';
UPDATE producto SET imagen_url = 'https://images.unsplash.com/photo-1542826438-bd32f43d626f?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80' WHERE slug = 'porcion-tres-leches';
UPDATE producto SET imagen_url = 'https://images.unsplash.com/photo-1586788224331-9a74aa9a7810?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80' WHERE slug = 'porcion-red-velvet';
UPDATE producto SET imagen_url = 'https://images.unsplash.com/photo-1606313564200-e75d5e30476c?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80' WHERE slug = 'brownie-clasico';
UPDATE producto SET imagen_url = 'https://images.unsplash.com/photo-1565958011703-44f9829ba187?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80' WHERE slug = 'tarta-de-frutas';
