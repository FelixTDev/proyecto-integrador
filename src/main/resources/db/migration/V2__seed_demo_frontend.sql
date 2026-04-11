SET NAMES utf8mb4;

INSERT INTO rol (nombre)
SELECT 'ADMIN' WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'ADMIN');
INSERT INTO rol (nombre)
SELECT 'VENDEDOR' WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'VENDEDOR');
INSERT INTO rol (nombre)
SELECT 'CLIENTE' WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'CLIENTE');

INSERT INTO usuario (nombre, email, password_hash, telefono, activo, fecha_verificacion_email)
SELECT 'Administrador Casa Chantilly', 'admin@casachantilly.pe', '$2a$10$zIL7lAtje1Kki9scobwhK.nPlZ6F/Z.1E3ycT4nh7R9.ZOrwQ7CEG', '999100200', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'admin@casachantilly.pe');

INSERT INTO usuario (nombre, email, password_hash, telefono, activo, fecha_verificacion_email)
SELECT 'Vendedor Mostrador', 'vendedor@casachantilly.pe', '$2a$10$cY6unTWRM.VAqZMjvKFztuyzPdLyS/1buPhXTnSeWcdAC49ZhIqb6', '999300400', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'vendedor@casachantilly.pe');

INSERT INTO usuario (nombre, email, password_hash, telefono, activo, fecha_verificacion_email)
SELECT 'Lucia Rojas', 'cliente1@casachantilly.pe', '$2a$10$eGXGrNpE3RzsH/o3XrTy5eAmoGp020c3Z0TQ1plchA.oojAdppCp2', '987654321', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'cliente1@casachantilly.pe');

INSERT INTO usuario (nombre, email, password_hash, telefono, activo, fecha_verificacion_email)
SELECT 'Martin Paredes', 'cliente2@casachantilly.pe', '$2a$10$FvKANy1nJ48EVghEW2.rIu.rssMb2TlJNDoFRA8ZreFnBzefBqL2G', '989123456', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'cliente2@casachantilly.pe');

INSERT INTO usuario (nombre, email, password_hash, telefono, activo, fecha_verificacion_email)
SELECT 'Camila Torres', 'cliente3@casachantilly.pe', '$2a$10$dT4KSlGqHfUaq.i/JsyBReKHpWJybBk4nCFT/FzHE3s1qLl8mSOZe', '986456789', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'cliente3@casachantilly.pe');

INSERT INTO usuario_rol (usuario_id, rol_id)
SELECT u.id, r.id FROM usuario u JOIN rol r ON r.nombre = 'ADMIN'
WHERE u.email = 'admin@casachantilly.pe'
  AND NOT EXISTS (SELECT 1 FROM usuario_rol ur WHERE ur.usuario_id = u.id AND ur.rol_id = r.id);

INSERT INTO usuario_rol (usuario_id, rol_id)
SELECT u.id, r.id FROM usuario u JOIN rol r ON r.nombre = 'VENDEDOR'
WHERE u.email = 'vendedor@casachantilly.pe'
  AND NOT EXISTS (SELECT 1 FROM usuario_rol ur WHERE ur.usuario_id = u.id AND ur.rol_id = r.id);

INSERT INTO usuario_rol (usuario_id, rol_id)
SELECT u.id, r.id FROM usuario u JOIN rol r ON r.nombre = 'CLIENTE'
WHERE u.email IN ('cliente1@casachantilly.pe','cliente2@casachantilly.pe','cliente3@casachantilly.pe')
  AND NOT EXISTS (SELECT 1 FROM usuario_rol ur WHERE ur.usuario_id = u.id AND ur.rol_id = r.id);

INSERT INTO categoria (nombre, slug, activo)
SELECT 'Tortas Enteras', 'tortas-enteras', 1 WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE slug = 'tortas-enteras');
INSERT INTO categoria (nombre, slug, activo)
SELECT 'Porciones', 'porciones', 1 WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE slug = 'porciones');
INSERT INTO categoria (nombre, slug, activo)
SELECT 'Bocaditos', 'bocaditos', 1 WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE slug = 'bocaditos');
INSERT INTO categoria (nombre, slug, activo)
SELECT 'Postres Especiales', 'postres-especiales', 1 WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE slug = 'postres-especiales');

INSERT INTO alergeno (nombre)
SELECT 'Gluten' WHERE NOT EXISTS (SELECT 1 FROM alergeno WHERE nombre = 'Gluten');
INSERT INTO alergeno (nombre)
SELECT 'Lactosa' WHERE NOT EXISTS (SELECT 1 FROM alergeno WHERE nombre = 'Lactosa');
INSERT INTO alergeno (nombre)
SELECT 'Huevo' WHERE NOT EXISTS (SELECT 1 FROM alergeno WHERE nombre = 'Huevo');
INSERT INTO alergeno (nombre)
SELECT 'Frutos secos' WHERE NOT EXISTS (SELECT 1 FROM alergeno WHERE nombre = 'Frutos secos');

INSERT INTO zona_envio (nombre_distrito, costo_delivery, tiempo_estimado_min, activo)
SELECT 'Miraflores', 8.50, 45, 1 WHERE NOT EXISTS (SELECT 1 FROM zona_envio WHERE nombre_distrito='Miraflores');
INSERT INTO zona_envio (nombre_distrito, costo_delivery, tiempo_estimado_min, activo)
SELECT 'San Isidro', 9.50, 50, 1 WHERE NOT EXISTS (SELECT 1 FROM zona_envio WHERE nombre_distrito='San Isidro');
INSERT INTO zona_envio (nombre_distrito, costo_delivery, tiempo_estimado_min, activo)
SELECT 'Surco', 11.00, 60, 1 WHERE NOT EXISTS (SELECT 1 FROM zona_envio WHERE nombre_distrito='Surco');
INSERT INTO zona_envio (nombre_distrito, costo_delivery, tiempo_estimado_min, activo)
SELECT 'La Molina', 12.50, 70, 1 WHERE NOT EXISTS (SELECT 1 FROM zona_envio WHERE nombre_distrito='La Molina');

INSERT INTO franja_horaria (fecha, hora_inicio, hora_fin, cupos_totales, cupos_ocupados, tipo)
SELECT DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00', '11:00:00', 15, 0, 'AMBOS'
WHERE NOT EXISTS (SELECT 1 FROM franja_horaria WHERE fecha = DATE_ADD(CURDATE(), INTERVAL 1 DAY) AND hora_inicio='09:00:00' AND hora_fin='11:00:00');
INSERT INTO franja_horaria (fecha, hora_inicio, hora_fin, cupos_totales, cupos_ocupados, tipo)
SELECT DATE_ADD(CURDATE(), INTERVAL 1 DAY), '13:00:00', '15:00:00', 15, 0, 'AMBOS'
WHERE NOT EXISTS (SELECT 1 FROM franja_horaria WHERE fecha = DATE_ADD(CURDATE(), INTERVAL 1 DAY) AND hora_inicio='13:00:00' AND hora_fin='15:00:00');
INSERT INTO franja_horaria (fecha, hora_inicio, hora_fin, cupos_totales, cupos_ocupados, tipo)
SELECT DATE_ADD(CURDATE(), INTERVAL 1 DAY), '18:00:00', '20:00:00', 15, 0, 'AMBOS'
WHERE NOT EXISTS (SELECT 1 FROM franja_horaria WHERE fecha = DATE_ADD(CURDATE(), INTERVAL 1 DAY) AND hora_inicio='18:00:00' AND hora_fin='20:00:00');

INSERT INTO producto (categoria_id, nombre, descripcion, activo, slug, imagen_url)
SELECT (SELECT id FROM categoria WHERE slug='tortas-enteras' LIMIT 1), 'Selva Negra Clasica', 'Torta de chocolate con crema batida y cerezas.', 1, 'selva-negra-clasica', 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?auto=format&fit=crop&w=800&q=80'
WHERE NOT EXISTS (SELECT 1 FROM producto WHERE slug='selva-negra-clasica');

INSERT INTO producto (categoria_id, nombre, descripcion, activo, slug, imagen_url)
SELECT (SELECT id FROM categoria WHERE slug='tortas-enteras' LIMIT 1), 'Cheesecake de Maracuya', 'Cheesecake cremoso con salsa de maracuya.', 1, 'cheesecake-maracuya', 'https://images.unsplash.com/photo-1533134242443-d4fd215305ad?auto=format&fit=crop&w=800&q=80'
WHERE NOT EXISTS (SELECT 1 FROM producto WHERE slug='cheesecake-maracuya');

INSERT INTO producto (categoria_id, nombre, descripcion, activo, slug, imagen_url)
SELECT (SELECT id FROM categoria WHERE slug='porciones' LIMIT 1), 'Porcion Tres Leches', 'Bizcocho humedo con tres leches.', 1, 'porcion-tres-leches', 'https://images.unsplash.com/photo-1542826438-bd32f43d626f?auto=format&fit=crop&w=800&q=80'
WHERE NOT EXISTS (SELECT 1 FROM producto WHERE slug='porcion-tres-leches');

INSERT INTO producto (categoria_id, nombre, descripcion, activo, slug, imagen_url)
SELECT (SELECT id FROM categoria WHERE slug='porciones' LIMIT 1), 'Porcion Red Velvet', 'Porcion individual con frosting de queso crema.', 1, 'porcion-red-velvet', 'https://images.unsplash.com/photo-1586788224331-9a74aa9a7810?auto=format&fit=crop&w=800&q=80'
WHERE NOT EXISTS (SELECT 1 FROM producto WHERE slug='porcion-red-velvet');

INSERT INTO producto (categoria_id, nombre, descripcion, activo, slug, imagen_url)
SELECT (SELECT id FROM categoria WHERE slug='bocaditos' LIMIT 1), 'Brownie Clasico', 'Brownie artesanal de cacao intenso.', 1, 'brownie-clasico', 'https://images.unsplash.com/photo-1606313564200-e75d5e30476c?auto=format&fit=crop&w=800&q=80'
WHERE NOT EXISTS (SELECT 1 FROM producto WHERE slug='brownie-clasico');

INSERT INTO producto (categoria_id, nombre, descripcion, activo, slug, imagen_url)
SELECT (SELECT id FROM categoria WHERE slug='postres-especiales' LIMIT 1), 'Tarta de Frutas', 'Masa sablee con crema pastelera y fruta fresca.', 1, 'tarta-de-frutas', 'https://images.unsplash.com/photo-1565958011703-44f9829ba187?auto=format&fit=crop&w=800&q=80'
WHERE NOT EXISTS (SELECT 1 FROM producto WHERE slug='tarta-de-frutas');

INSERT INTO producto_variante (producto_id, nombre_variante, precio, costo, peso_gramos, tiempo_prep_min, stock_disponible, activo, codigo_sku, codigo_barras)
SELECT (SELECT id FROM producto WHERE slug='selva-negra-clasica' LIMIT 1), 'Entera 20cm', 95.00, 48.00, 1800, 180, 12, 1, 'SKU-SN-ENT-01', '7750000000011'
WHERE NOT EXISTS (SELECT 1 FROM producto_variante WHERE codigo_sku='SKU-SN-ENT-01');

INSERT INTO producto_variante (producto_id, nombre_variante, precio, costo, peso_gramos, tiempo_prep_min, stock_disponible, activo, codigo_sku, codigo_barras)
SELECT (SELECT id FROM producto WHERE slug='cheesecake-maracuya' LIMIT 1), 'Entera 18cm', 88.00, 43.00, 1500, 160, 8, 1, 'SKU-CHM-ENT-01', '7750000000021'
WHERE NOT EXISTS (SELECT 1 FROM producto_variante WHERE codigo_sku='SKU-CHM-ENT-01');

INSERT INTO producto_variante (producto_id, nombre_variante, precio, costo, peso_gramos, tiempo_prep_min, stock_disponible, activo, codigo_sku, codigo_barras)
SELECT (SELECT id FROM producto WHERE slug='porcion-tres-leches' LIMIT 1), 'Porcion Individual', 12.00, 5.50, 170, 18, 25, 1, 'SKU-3L-POR-01', '7750000000031'
WHERE NOT EXISTS (SELECT 1 FROM producto_variante WHERE codigo_sku='SKU-3L-POR-01');

INSERT INTO producto_variante (producto_id, nombre_variante, precio, costo, peso_gramos, tiempo_prep_min, stock_disponible, activo, codigo_sku, codigo_barras)
SELECT (SELECT id FROM producto WHERE slug='porcion-red-velvet' LIMIT 1), 'Porcion Individual', 13.50, 6.20, 170, 18, 18, 1, 'SKU-RV-POR-01', '7750000000041'
WHERE NOT EXISTS (SELECT 1 FROM producto_variante WHERE codigo_sku='SKU-RV-POR-01');

INSERT INTO producto_variante (producto_id, nombre_variante, precio, costo, peso_gramos, tiempo_prep_min, stock_disponible, activo, codigo_sku, codigo_barras)
SELECT (SELECT id FROM producto WHERE slug='brownie-clasico' LIMIT 1), 'Caja x6', 38.00, 19.00, 600, 45, 16, 1, 'SKU-BR-X6-01', '7750000000051'
WHERE NOT EXISTS (SELECT 1 FROM producto_variante WHERE codigo_sku='SKU-BR-X6-01');

INSERT INTO producto_variante (producto_id, nombre_variante, precio, costo, peso_gramos, tiempo_prep_min, stock_disponible, activo, codigo_sku, codigo_barras)
SELECT (SELECT id FROM producto WHERE slug='tarta-de-frutas' LIMIT 1), 'Mediana', 72.00, 36.00, 1200, 140, 10, 1, 'SKU-TF-MED-01', '7750000000061'
WHERE NOT EXISTS (SELECT 1 FROM producto_variante WHERE codigo_sku='SKU-TF-MED-01');

INSERT INTO producto_alergeno (producto_id, alergeno_id)
SELECT p.id, a.id
FROM producto p
JOIN alergeno a ON a.nombre IN ('Gluten','Lactosa','Huevo')
WHERE p.slug IN ('selva-negra-clasica','cheesecake-maracuya','porcion-tres-leches','porcion-red-velvet','brownie-clasico','tarta-de-frutas')
  AND NOT EXISTS (SELECT 1 FROM producto_alergeno pa WHERE pa.producto_id=p.id AND pa.alergeno_id=a.id);

INSERT INTO promocion (nombre, tipo_descuento, valor_descuento, monto_minimo, aplica_a, fecha_inicio, fecha_fin, activo, codigo_cupon, limite_usos_total, limite_usos_por_usuario)
SELECT 'Campana Otono', 'PORCENTAJE', 12.00, 80.00, 'CARRITO', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 1, 'OTONO12', 500, 2
WHERE NOT EXISTS (SELECT 1 FROM promocion WHERE codigo_cupon='OTONO12');

INSERT INTO promocion (nombre, tipo_descuento, valor_descuento, monto_minimo, aplica_a, fecha_inicio, fecha_fin, activo, codigo_cupon, limite_usos_total, limite_usos_por_usuario)
SELECT 'Envio Cero Lima', 'ENVIO_GRATIS', 0.00, 60.00, 'CARRITO', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 1, 'ENVIO0LIM', 400, 3
WHERE NOT EXISTS (SELECT 1 FROM promocion WHERE codigo_cupon='ENVIO0LIM');

INSERT INTO producto_promocion (variante_id, promocion_id)
SELECT v.id, p.id
FROM producto_variante v
JOIN promocion p ON p.codigo_cupon = 'OTONO12'
WHERE v.codigo_sku IN ('SKU-SN-ENT-01','SKU-CHM-ENT-01','SKU-TF-MED-01')
  AND NOT EXISTS (SELECT 1 FROM producto_promocion pp WHERE pp.variante_id=v.id AND pp.promocion_id=p.id);

INSERT INTO metodo_pago (nombre, activo)
SELECT 'Tarjeta de credito/debito', 1 WHERE NOT EXISTS (SELECT 1 FROM metodo_pago WHERE nombre='Tarjeta de credito/debito');
INSERT INTO metodo_pago (nombre, activo)
SELECT 'Yape', 1 WHERE NOT EXISTS (SELECT 1 FROM metodo_pago WHERE nombre='Yape');
INSERT INTO metodo_pago (nombre, activo)
SELECT 'Plin', 1 WHERE NOT EXISTS (SELECT 1 FROM metodo_pago WHERE nombre='Plin');
INSERT INTO metodo_pago (nombre, activo)
SELECT 'Efectivo contra entrega', 1 WHERE NOT EXISTS (SELECT 1 FROM metodo_pago WHERE nombre='Efectivo contra entrega');
INSERT INTO metodo_pago (nombre, activo)
SELECT 'Transferencia bancaria', 1 WHERE NOT EXISTS (SELECT 1 FROM metodo_pago WHERE nombre='Transferencia bancaria');

INSERT INTO estado_pedido (nombre, orden)
SELECT 'Pendiente de pago', 1 WHERE NOT EXISTS (SELECT 1 FROM estado_pedido WHERE nombre='Pendiente de pago');
INSERT INTO estado_pedido (nombre, orden)
SELECT 'Pago confirmado', 2 WHERE NOT EXISTS (SELECT 1 FROM estado_pedido WHERE nombre='Pago confirmado');
INSERT INTO estado_pedido (nombre, orden)
SELECT 'En preparacion', 3 WHERE NOT EXISTS (SELECT 1 FROM estado_pedido WHERE nombre='En preparacion');
INSERT INTO estado_pedido (nombre, orden)
SELECT 'Listo para recoger', 4 WHERE NOT EXISTS (SELECT 1 FROM estado_pedido WHERE nombre='Listo para recoger');
INSERT INTO estado_pedido (nombre, orden)
SELECT 'En ruta', 5 WHERE NOT EXISTS (SELECT 1 FROM estado_pedido WHERE nombre='En ruta');
INSERT INTO estado_pedido (nombre, orden)
SELECT 'Entregado', 6 WHERE NOT EXISTS (SELECT 1 FROM estado_pedido WHERE nombre='Entregado');
INSERT INTO estado_pedido (nombre, orden)
SELECT 'Cancelado', 7 WHERE NOT EXISTS (SELECT 1 FROM estado_pedido WHERE nombre='Cancelado');
INSERT INTO estado_pedido (nombre, orden)
SELECT 'Rechazado', 8 WHERE NOT EXISTS (SELECT 1 FROM estado_pedido WHERE nombre='Rechazado');

SET @admin_id := (SELECT id FROM usuario WHERE email='admin@casachantilly.pe' LIMIT 1);
SET @vend_id := (SELECT id FROM usuario WHERE email='vendedor@casachantilly.pe' LIMIT 1);
SET @cli1_id := (SELECT id FROM usuario WHERE email='cliente1@casachantilly.pe' LIMIT 1);
SET @cli2_id := (SELECT id FROM usuario WHERE email='cliente2@casachantilly.pe' LIMIT 1);
SET @cli3_id := (SELECT id FROM usuario WHERE email='cliente3@casachantilly.pe' LIMIT 1);

SET @zona_mira := (SELECT id FROM zona_envio WHERE nombre_distrito='Miraflores' LIMIT 1);
SET @zona_surco := (SELECT id FROM zona_envio WHERE nombre_distrito='Surco' LIMIT 1);

INSERT INTO direccion (usuario_id, zona_id, etiqueta, direccion_completa, referencia, destinatario_nombre, destinatario_telefono, activo, es_principal, latitud, longitud)
SELECT @cli1_id, @zona_mira, 'Casa', 'Av. Pardo 123, Miraflores', 'Dpto 402', 'Lucia Rojas', '987654321', 1, 1, -12.1212, -77.0297
WHERE @cli1_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM direccion WHERE usuario_id=@cli1_id AND etiqueta='Casa');

INSERT INTO direccion (usuario_id, zona_id, etiqueta, direccion_completa, referencia, destinatario_nombre, destinatario_telefono, activo, es_principal, latitud, longitud)
SELECT @cli2_id, @zona_surco, 'Casa', 'Jr. Monte Rosa 880, Surco', 'Casa azul puerta blanca', 'Martin Paredes', '989123456', 1, 1, -12.1336, -76.9912
WHERE @cli2_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM direccion WHERE usuario_id=@cli2_id AND etiqueta='Casa');

INSERT INTO direccion (usuario_id, zona_id, etiqueta, direccion_completa, referencia, destinatario_nombre, destinatario_telefono, activo, es_principal, latitud, longitud)
SELECT @cli3_id, @zona_mira, 'Oficina', 'Calle Schell 512, Miraflores', 'Recepcion edificio', 'Camila Torres', '986456789', 1, 1, -12.1240, -77.0320
WHERE @cli3_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM direccion WHERE usuario_id=@cli3_id AND etiqueta='Oficina');

SET @dir_cli1 := (SELECT id FROM direccion WHERE usuario_id=@cli1_id ORDER BY es_principal DESC, id ASC LIMIT 1);
SET @dir_cli2 := (SELECT id FROM direccion WHERE usuario_id=@cli2_id ORDER BY es_principal DESC, id ASC LIMIT 1);
SET @dir_cli3 := (SELECT id FROM direccion WHERE usuario_id=@cli3_id ORDER BY es_principal DESC, id ASC LIMIT 1);
SET @franja_ref := (SELECT id FROM franja_horaria WHERE fecha = DATE_ADD(CURDATE(), INTERVAL 1 DAY) ORDER BY hora_inicio LIMIT 1);
SET @promo_oto := (SELECT id FROM promocion WHERE codigo_cupon='OTONO12' LIMIT 1);

INSERT INTO pedido (usuario_id, direccion_id, franja_horaria_id, promocion_id, subtotal, descuento, costo_envio, impuestos, total, es_recojo_tienda, fecha_creacion, codigo_pedido, estado_actual_id, fecha_actualizacion)
SELECT @cli1_id, @dir_cli1, @franja_ref, @promo_oto, 107.00, 12.00, 8.50, 17.10, 120.60, 0, DATE_SUB(NOW(), INTERVAL 4 DAY), 'PED-DEMO-2001', 6, DATE_SUB(NOW(), INTERVAL 3 DAY)
WHERE @cli1_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pedido WHERE codigo_pedido='PED-DEMO-2001');

INSERT INTO pedido (usuario_id, direccion_id, franja_horaria_id, promocion_id, subtotal, descuento, costo_envio, impuestos, total, es_recojo_tienda, fecha_creacion, codigo_pedido, estado_actual_id, fecha_actualizacion)
SELECT @cli2_id, @dir_cli2, @franja_ref, NULL, 88.00, 0.00, 9.50, 15.84, 113.34, 0, DATE_SUB(NOW(), INTERVAL 2 DAY), 'PED-DEMO-2002', 5, DATE_SUB(NOW(), INTERVAL 1 DAY)
WHERE @cli2_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pedido WHERE codigo_pedido='PED-DEMO-2002');

INSERT INTO pedido (usuario_id, direccion_id, franja_horaria_id, promocion_id, subtotal, descuento, costo_envio, impuestos, total, es_recojo_tienda, fecha_creacion, codigo_pedido, estado_actual_id, fecha_actualizacion)
SELECT @cli3_id, NULL, @franja_ref, NULL, 51.50, 0.00, 0.00, 9.27, 60.77, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), 'PED-DEMO-2003', 2, DATE_SUB(NOW(), INTERVAL 1 DAY)
WHERE @cli3_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pedido WHERE codigo_pedido='PED-DEMO-2003');

INSERT INTO pedido (usuario_id, direccion_id, franja_horaria_id, promocion_id, subtotal, descuento, costo_envio, impuestos, total, es_recojo_tienda, fecha_creacion, codigo_pedido, estado_actual_id, fecha_actualizacion)
SELECT @cli1_id, @dir_cli1, @franja_ref, NULL, 38.00, 0.00, 8.50, 6.84, 53.34, 0, DATE_SUB(NOW(), INTERVAL 1 DAY), 'PED-DEMO-2004', 7, DATE_SUB(NOW(), INTERVAL 12 HOUR)
WHERE @cli1_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pedido WHERE codigo_pedido='PED-DEMO-2004');

SET @ped1 := (SELECT id FROM pedido WHERE codigo_pedido='PED-DEMO-2001' LIMIT 1);
SET @ped2 := (SELECT id FROM pedido WHERE codigo_pedido='PED-DEMO-2002' LIMIT 1);
SET @ped3 := (SELECT id FROM pedido WHERE codigo_pedido='PED-DEMO-2003' LIMIT 1);
SET @ped4 := (SELECT id FROM pedido WHERE codigo_pedido='PED-DEMO-2004' LIMIT 1);

SET @v_selva := (SELECT id FROM producto_variante WHERE codigo_sku='SKU-SN-ENT-01' LIMIT 1);
SET @v_tresl := (SELECT id FROM producto_variante WHERE codigo_sku='SKU-3L-POR-01' LIMIT 1);
SET @v_chees := (SELECT id FROM producto_variante WHERE codigo_sku='SKU-CHM-ENT-01' LIMIT 1);
SET @v_red := (SELECT id FROM producto_variante WHERE codigo_sku='SKU-RV-POR-01' LIMIT 1);
SET @v_brown := (SELECT id FROM producto_variante WHERE codigo_sku='SKU-BR-X6-01' LIMIT 1);

INSERT INTO detalle_pedido (pedido_id, variante_id, nombre_snapshot, precio_unitario_snapshot, cantidad, subtotal_linea)
SELECT @ped1, @v_selva, 'Selva Negra Clasica - Entera 20cm', 95.00, 1, 95.00
WHERE @ped1 IS NOT NULL AND @v_selva IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM detalle_pedido WHERE pedido_id=@ped1 AND variante_id=@v_selva);

INSERT INTO detalle_pedido (pedido_id, variante_id, nombre_snapshot, precio_unitario_snapshot, cantidad, subtotal_linea)
SELECT @ped1, @v_tresl, 'Porcion Tres Leches - Porcion Individual', 12.00, 1, 12.00
WHERE @ped1 IS NOT NULL AND @v_tresl IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM detalle_pedido WHERE pedido_id=@ped1 AND variante_id=@v_tresl);

INSERT INTO detalle_pedido (pedido_id, variante_id, nombre_snapshot, precio_unitario_snapshot, cantidad, subtotal_linea)
SELECT @ped2, @v_chees, 'Cheesecake de Maracuya - Entera 18cm', 88.00, 1, 88.00
WHERE @ped2 IS NOT NULL AND @v_chees IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM detalle_pedido WHERE pedido_id=@ped2 AND variante_id=@v_chees);

INSERT INTO detalle_pedido (pedido_id, variante_id, nombre_snapshot, precio_unitario_snapshot, cantidad, subtotal_linea)
SELECT @ped3, @v_brown, 'Brownie Clasico - Caja x6', 38.00, 1, 38.00
WHERE @ped3 IS NOT NULL AND @v_brown IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM detalle_pedido WHERE pedido_id=@ped3 AND variante_id=@v_brown);

INSERT INTO detalle_pedido (pedido_id, variante_id, nombre_snapshot, precio_unitario_snapshot, cantidad, subtotal_linea)
SELECT @ped3, @v_red, 'Porcion Red Velvet - Porcion Individual', 13.50, 1, 13.50
WHERE @ped3 IS NOT NULL AND @v_red IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM detalle_pedido WHERE pedido_id=@ped3 AND variante_id=@v_red);

INSERT INTO detalle_pedido (pedido_id, variante_id, nombre_snapshot, precio_unitario_snapshot, cantidad, subtotal_linea)
SELECT @ped4, @v_brown, 'Brownie Clasico - Caja x6', 38.00, 1, 38.00
WHERE @ped4 IS NOT NULL AND @v_brown IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM detalle_pedido WHERE pedido_id=@ped4 AND variante_id=@v_brown);

INSERT INTO estado_pedido_historial (pedido_id, estado_id, usuario_id, observacion, fecha)
SELECT @ped1, 1, @cli1_id, 'Pedido creado', DATE_SUB(NOW(), INTERVAL 4 DAY)
WHERE @ped1 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM estado_pedido_historial WHERE pedido_id=@ped1 AND estado_id=1);
INSERT INTO estado_pedido_historial (pedido_id, estado_id, usuario_id, observacion, fecha)
SELECT @ped1, 2, @admin_id, 'Pago confirmado por administracion', DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 20 MINUTE
WHERE @ped1 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM estado_pedido_historial WHERE pedido_id=@ped1 AND estado_id=2);
INSERT INTO estado_pedido_historial (pedido_id, estado_id, usuario_id, observacion, fecha)
SELECT @ped1, 6, @vend_id, 'Pedido entregado al cliente', DATE_SUB(NOW(), INTERVAL 3 DAY)
WHERE @ped1 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM estado_pedido_historial WHERE pedido_id=@ped1 AND estado_id=6);

INSERT INTO estado_pedido_historial (pedido_id, estado_id, usuario_id, observacion, fecha)
SELECT @ped2, 1, @cli2_id, 'Pedido creado', DATE_SUB(NOW(), INTERVAL 2 DAY)
WHERE @ped2 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM estado_pedido_historial WHERE pedido_id=@ped2 AND estado_id=1);
INSERT INTO estado_pedido_historial (pedido_id, estado_id, usuario_id, observacion, fecha)
SELECT @ped2, 2, @admin_id, 'Pago confirmado', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 15 MINUTE
WHERE @ped2 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM estado_pedido_historial WHERE pedido_id=@ped2 AND estado_id=2);
INSERT INTO estado_pedido_historial (pedido_id, estado_id, usuario_id, observacion, fecha)
SELECT @ped2, 5, @vend_id, 'Pedido en ruta', DATE_SUB(NOW(), INTERVAL 1 DAY)
WHERE @ped2 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM estado_pedido_historial WHERE pedido_id=@ped2 AND estado_id=5);

INSERT INTO estado_pedido_historial (pedido_id, estado_id, usuario_id, observacion, fecha)
SELECT @ped3, 1, @cli3_id, 'Pedido creado', DATE_SUB(NOW(), INTERVAL 1 DAY)
WHERE @ped3 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM estado_pedido_historial WHERE pedido_id=@ped3 AND estado_id=1);
INSERT INTO estado_pedido_historial (pedido_id, estado_id, usuario_id, observacion, fecha)
SELECT @ped3, 2, @admin_id, 'Pago confirmado', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 10 MINUTE
WHERE @ped3 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM estado_pedido_historial WHERE pedido_id=@ped3 AND estado_id=2);

INSERT INTO estado_pedido_historial (pedido_id, estado_id, usuario_id, observacion, fecha)
SELECT @ped4, 1, @cli1_id, 'Pedido creado', DATE_SUB(NOW(), INTERVAL 1 DAY)
WHERE @ped4 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM estado_pedido_historial WHERE pedido_id=@ped4 AND estado_id=1);
INSERT INTO estado_pedido_historial (pedido_id, estado_id, usuario_id, observacion, fecha)
SELECT @ped4, 7, @admin_id, 'Cancelado por solicitud del cliente', DATE_SUB(NOW(), INTERVAL 12 HOUR)
WHERE @ped4 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM estado_pedido_historial WHERE pedido_id=@ped4 AND estado_id=7);

SET @mp_card := (SELECT id FROM metodo_pago WHERE nombre='Tarjeta de credito/debito' LIMIT 1);
SET @mp_yape := (SELECT id FROM metodo_pago WHERE nombre='Yape' LIMIT 1);
SET @mp_cash := (SELECT id FROM metodo_pago WHERE nombre='Efectivo contra entrega' LIMIT 1);

INSERT INTO pago (pedido_id, metodo_pago_id, monto, estado, referencia_externa, intentos, fecha, moneda, id_transaccion_externa, fecha_aprobacion)
SELECT @ped1, @mp_card, 120.60, 'APROBADO', 'Cobro tarjeta demo PED-DEMO-2001', 1, DATE_SUB(NOW(), INTERVAL 4 DAY), 'PEN', 'TRX-DEMO-2001', DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 5 MINUTE
WHERE @ped1 IS NOT NULL AND @mp_card IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pago WHERE pedido_id=@ped1);

INSERT INTO pago (pedido_id, metodo_pago_id, monto, estado, referencia_externa, intentos, fecha, moneda, id_transaccion_externa, fecha_aprobacion)
SELECT @ped2, @mp_yape, 113.34, 'APROBADO', 'Pago yape demo PED-DEMO-2002', 1, DATE_SUB(NOW(), INTERVAL 2 DAY), 'PEN', 'TRX-DEMO-2002', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 6 MINUTE
WHERE @ped2 IS NOT NULL AND @mp_yape IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pago WHERE pedido_id=@ped2);

INSERT INTO pago (pedido_id, metodo_pago_id, monto, estado, referencia_externa, intentos, fecha, moneda, id_transaccion_externa, fecha_aprobacion)
SELECT @ped3, @mp_cash, 60.77, 'PENDIENTE', 'Pago contra entrega PED-DEMO-2003', 0, DATE_SUB(NOW(), INTERVAL 1 DAY), 'PEN', 'TRX-DEMO-2003', NULL
WHERE @ped3 IS NOT NULL AND @mp_cash IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pago WHERE pedido_id=@ped3);

INSERT INTO pago (pedido_id, metodo_pago_id, monto, estado, referencia_externa, intentos, fecha, moneda, id_transaccion_externa, fecha_aprobacion)
SELECT @ped4, @mp_card, 53.34, 'RECHAZADO', 'Pago rechazado PED-DEMO-2004', 2, DATE_SUB(NOW(), INTERVAL 1 DAY), 'PEN', 'TRX-DEMO-2004', NULL
WHERE @ped4 IS NOT NULL AND @mp_card IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pago WHERE pedido_id=@ped4);

INSERT INTO carrito (usuario_id, promocion_id)
SELECT @cli2_id, @promo_oto
WHERE @cli2_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM carrito WHERE usuario_id=@cli2_id);

INSERT INTO carrito (usuario_id, promocion_id)
SELECT @cli3_id, NULL
WHERE @cli3_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM carrito WHERE usuario_id=@cli3_id);

SET @car_cli2 := (SELECT id FROM carrito WHERE usuario_id=@cli2_id LIMIT 1);
SET @car_cli3 := (SELECT id FROM carrito WHERE usuario_id=@cli3_id LIMIT 1);

INSERT INTO carrito_detalle (carrito_id, variante_id, cantidad)
SELECT @car_cli2, @v_tresl, 2
WHERE @car_cli2 IS NOT NULL AND @v_tresl IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM carrito_detalle WHERE carrito_id=@car_cli2 AND variante_id=@v_tresl);

INSERT INTO carrito_detalle (carrito_id, variante_id, cantidad)
SELECT @car_cli2, @v_red, 1
WHERE @car_cli2 IS NOT NULL AND @v_red IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM carrito_detalle WHERE carrito_id=@car_cli2 AND variante_id=@v_red);

INSERT INTO carrito_detalle (carrito_id, variante_id, cantidad)
SELECT @car_cli3, @v_chees, 1
WHERE @car_cli3 IS NOT NULL AND @v_chees IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM carrito_detalle WHERE carrito_id=@car_cli3 AND variante_id=@v_chees);

INSERT INTO reclamo (pedido_id, usuario_id, tipo, descripcion, estado, monto_reembolso, fecha_creacion, fecha_resolucion, fecha_actualizacion, prioridad, detalle_resolucion)
SELECT @ped1, @cli1_id, 'REPOSICION', 'El empaque llego danado, pero el producto estaba conforme.', 'RESUELTO', 0.00,
       DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 36 HOUR), DATE_SUB(NOW(), INTERVAL 36 HOUR), 'MEDIA', 'Se envio reposicion de empaque y cupon de cortesia.'
WHERE @ped1 IS NOT NULL AND @cli1_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM reclamo WHERE pedido_id=@ped1 AND usuario_id=@cli1_id);

INSERT INTO chat_mensaje (pedido_id, usuario_id, mensaje, leido, fecha)
SELECT @ped2, @cli2_id, 'Hola, a que hora estiman la llegada de mi pedido?', 1, DATE_SUB(NOW(), INTERVAL 22 HOUR)
WHERE @ped2 IS NOT NULL AND @cli2_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM chat_mensaje WHERE pedido_id=@ped2 AND usuario_id=@cli2_id AND mensaje='Hola, a que hora estiman la llegada de mi pedido?');

INSERT INTO chat_mensaje (pedido_id, usuario_id, mensaje, leido, fecha)
SELECT @ped2, @vend_id, 'Llegamos en aproximadamente 35 minutos. Ya salio de tienda.', 0, DATE_SUB(NOW(), INTERVAL 21 HOUR)
WHERE @ped2 IS NOT NULL AND @vend_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM chat_mensaje WHERE pedido_id=@ped2 AND usuario_id=@vend_id AND mensaje='Llegamos en aproximadamente 35 minutos. Ya salio de tienda.');

INSERT INTO notificacion (usuario_id, pedido_id, canal, asunto, mensaje, leida, intentos, fecha_envio, estado_envio, destino_canal)
SELECT @cli1_id, @ped1, 'EMAIL', 'Pedido entregado', 'Tu pedido PED-DEMO-2001 fue entregado correctamente.', 0, 1, DATE_SUB(NOW(), INTERVAL 3 DAY), 'ENVIADA', 'cliente1@casachantilly.pe'
WHERE @cli1_id IS NOT NULL AND @ped1 IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM notificacion WHERE usuario_id=@cli1_id AND pedido_id=@ped1 AND asunto='Pedido entregado');

INSERT INTO notificacion (usuario_id, pedido_id, canal, asunto, mensaje, leida, intentos, fecha_envio, estado_envio, destino_canal)
SELECT @cli2_id, @ped2, 'WHATSAPP', 'Pedido en ruta', 'Tu pedido PED-DEMO-2002 esta en camino.', 0, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), 'ENVIADA', '+51989123456'
WHERE @cli2_id IS NOT NULL AND @ped2 IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM notificacion WHERE usuario_id=@cli2_id AND pedido_id=@ped2 AND asunto='Pedido en ruta');

INSERT INTO notificacion (usuario_id, pedido_id, canal, asunto, mensaje, leida, intentos, fecha_envio, estado_envio, destino_canal)
SELECT @cli3_id, @ped3, 'PUSH', 'Pago pendiente en tienda', 'Recuerda confirmar el pago al momento del recojo.', 0, 1, DATE_SUB(NOW(), INTERVAL 12 HOUR), 'PENDIENTE', 'app://cliente3'
WHERE @cli3_id IS NOT NULL AND @ped3 IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM notificacion WHERE usuario_id=@cli3_id AND pedido_id=@ped3 AND asunto='Pago pendiente en tienda');

INSERT INTO puntos_fidelidad (usuario_id, pedido_id, puntos, tipo, descripcion, fecha, saldo_resultante)
SELECT @cli1_id, @ped1, 12, 'GANADO', 'Puntos por compra PED-DEMO-2001', DATE_SUB(NOW(), INTERVAL 3 DAY), 12
WHERE @cli1_id IS NOT NULL AND @ped1 IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM puntos_fidelidad WHERE usuario_id=@cli1_id AND pedido_id=@ped1 AND tipo='GANADO');

INSERT INTO puntos_fidelidad (usuario_id, pedido_id, puntos, tipo, descripcion, fecha, saldo_resultante)
SELECT @cli1_id, @ped4, -5, 'CANJEADO', 'Canje parcial aplicado al pedido PED-DEMO-2004', DATE_SUB(NOW(), INTERVAL 1 DAY), 7
WHERE @cli1_id IS NOT NULL AND @ped4 IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM puntos_fidelidad WHERE usuario_id=@cli1_id AND pedido_id=@ped4 AND tipo='CANJEADO');

SET @detalle_personalizacion := (
  SELECT dp.id
  FROM detalle_pedido dp
  JOIN pedido p ON p.id = dp.pedido_id
  WHERE p.codigo_pedido='PED-DEMO-2001'
  ORDER BY dp.id
  LIMIT 1
);

INSERT INTO personalizacion (detalle_pedido_id, sabor_bizcocho, tipo_relleno, color_decorado, texto_pastel, notas_cliente, imagen_referencia_url)
SELECT @detalle_personalizacion, 'Chocolate amargo', 'Ganache de cacao 65%', 'Rojo vino y blanco', 'Feliz aniversario Lucia', 'Sin nueces y con acabado liso.', 'https://images.unsplash.com/photo-1559620192-032c4bc4674e?auto=format&fit=crop&w=900&q=80'
WHERE @detalle_personalizacion IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM personalizacion WHERE detalle_pedido_id=@detalle_personalizacion);


