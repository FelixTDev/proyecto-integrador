SET NAMES utf8mb4;
START TRANSACTION;

SET @cliente_id := (SELECT id FROM usuario WHERE email = 'cliente@gmail.com' LIMIT 1);
SET @admin_id := (SELECT id FROM usuario WHERE email = 'admin@casachantilly.pe' LIMIT 1);

/* 1) CATEGORIAS */
INSERT INTO categoria (nombre, slug, activo)
SELECT s.nombre, s.slug, s.activo
FROM (
  SELECT 'Tortas Enteras' AS nombre, 'tortas-enteras' AS slug, 1 AS activo
  UNION ALL SELECT 'Porciones', 'porciones', 1
  UNION ALL SELECT 'Bocaditos', 'bocaditos', 1
  UNION ALL SELECT 'Postres Especiales', 'postres-especiales', 1
) s
WHERE NOT EXISTS (
  SELECT 1 FROM categoria c WHERE c.slug = s.slug
);

/* 2) ZONAS DE ENVIO */
INSERT INTO zona_envio (nombre_distrito, costo_delivery, tiempo_estimado_min, activo)
SELECT s.nombre_distrito, s.costo_delivery, s.tiempo_estimado_min, s.activo
FROM (
  SELECT 'Miraflores' AS nombre_distrito, 8.50 AS costo_delivery, 45 AS tiempo_estimado_min, 1 AS activo
  UNION ALL SELECT 'San Isidro', 9.50, 50, 1
  UNION ALL SELECT 'Surco', 11.00, 60, 1
  UNION ALL SELECT 'La Molina', 12.50, 70, 1
) s
WHERE NOT EXISTS (
  SELECT 1 FROM zona_envio z WHERE z.nombre_distrito = s.nombre_distrito
);

/* 3) FRANJAS HORARIAS (proximos 3 dias) */
INSERT INTO franja_horaria (fecha, hora_inicio, hora_fin, cupos_totales, cupos_ocupados, tipo)
SELECT s.fecha, s.hora_inicio, s.hora_fin, s.cupos_totales, s.cupos_ocupados, s.tipo
FROM (
  SELECT DATE_ADD(CURDATE(), INTERVAL 1 DAY) AS fecha, '09:00:00' AS hora_inicio, '11:00:00' AS hora_fin, 12 AS cupos_totales, 0 AS cupos_ocupados, 'AMBOS' AS tipo
  UNION ALL SELECT DATE_ADD(CURDATE(), INTERVAL 1 DAY), '13:00:00', '15:00:00', 12, 0, 'AMBOS'
  UNION ALL SELECT DATE_ADD(CURDATE(), INTERVAL 1 DAY), '18:00:00', '20:00:00', 12, 0, 'AMBOS'
  UNION ALL SELECT DATE_ADD(CURDATE(), INTERVAL 2 DAY), '09:00:00', '11:00:00', 12, 0, 'AMBOS'
  UNION ALL SELECT DATE_ADD(CURDATE(), INTERVAL 2 DAY), '13:00:00', '15:00:00', 12, 0, 'AMBOS'
  UNION ALL SELECT DATE_ADD(CURDATE(), INTERVAL 2 DAY), '18:00:00', '20:00:00', 12, 0, 'AMBOS'
  UNION ALL SELECT DATE_ADD(CURDATE(), INTERVAL 3 DAY), '09:00:00', '11:00:00', 12, 0, 'AMBOS'
  UNION ALL SELECT DATE_ADD(CURDATE(), INTERVAL 3 DAY), '13:00:00', '15:00:00', 12, 0, 'AMBOS'
  UNION ALL SELECT DATE_ADD(CURDATE(), INTERVAL 3 DAY), '18:00:00', '20:00:00', 12, 0, 'AMBOS'
) s
WHERE NOT EXISTS (
  SELECT 1
  FROM franja_horaria f
  WHERE f.fecha = s.fecha
    AND f.hora_inicio = s.hora_inicio
    AND f.hora_fin = s.hora_fin
    AND f.tipo = s.tipo
);

/* 4) PRODUCTOS */
INSERT INTO producto (categoria_id, nombre, descripcion, activo, slug, imagen_url)
SELECT
  (SELECT id FROM categoria WHERE slug = s.cat_slug LIMIT 1) AS categoria_id,
  s.nombre, s.descripcion, 1, s.slug, s.imagen_url
FROM (
  SELECT 'tortas-enteras' AS cat_slug, 'Selva Negra Clasica' AS nombre, 'Torta de chocolate con crema y cerezas.' AS descripcion, 'selva-negra-clasica' AS slug, 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80' AS imagen_url
  UNION ALL SELECT 'tortas-enteras', 'Cheesecake de Maracuya', 'Cheesecake cremoso con coulis de maracuya.', 'cheesecake-maracuya', 'https://images.unsplash.com/photo-1533134242443-d4fd215305ad?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'
  UNION ALL SELECT 'porciones', 'Porcion Tres Leches', 'Bizcocho humedo con mezcla de tres leches.', 'porcion-tres-leches', 'https://images.unsplash.com/photo-1542826438-bd32f43d626f?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'
  UNION ALL SELECT 'porciones', 'Porcion Red Velvet', 'Porcion individual de red velvet con frosting.', 'porcion-red-velvet', 'https://images.unsplash.com/photo-1586788224331-9a74aa9a7810?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'
  UNION ALL SELECT 'bocaditos', 'Brownie Clasico', 'Brownie artesanal de cacao intenso.', 'brownie-clasico', 'https://images.unsplash.com/photo-1606313564200-e75d5e30476c?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'
  UNION ALL SELECT 'postres-especiales', 'Tarta de Frutas', 'Base crocante con crema pastelera y frutas frescas.', 'tarta-de-frutas', 'https://images.unsplash.com/photo-1565958011703-44f9829ba187?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'
) s
WHERE NOT EXISTS (
  SELECT 1 FROM producto p WHERE p.slug = s.slug
);

/* 5) VARIANTES */
INSERT INTO producto_variante (
  producto_id, nombre_variante, precio, costo, peso_gramos, tiempo_prep_min, stock_disponible, activo, codigo_sku, codigo_barras
)
SELECT
  (SELECT id FROM producto WHERE slug = s.prod_slug LIMIT 1) AS producto_id,
  s.nombre_variante, s.precio, s.costo, s.peso_gramos, s.tiempo_prep_min, s.stock_disponible, 1, s.codigo_sku, s.codigo_barras
FROM (
  SELECT 'selva-negra-clasica' AS prod_slug, 'Entera 20cm' AS nombre_variante, 95.00 AS precio, 48.00 AS costo, 1800 AS peso_gramos, 180 AS tiempo_prep_min, 6 AS stock_disponible, 'SKU-SN-ENT-01' AS codigo_sku, '7750000000011' AS codigo_barras
  UNION ALL SELECT 'selva-negra-clasica', 'Porcion', 14.50, 7.20, 180, 20, 0, 'SKU-SN-POR-01', '7750000000012'
  UNION ALL SELECT 'cheesecake-maracuya', 'Entera 18cm', 88.00, 43.00, 1500, 160, 4, 'SKU-CHM-ENT-01', '7750000000021'
  UNION ALL SELECT 'cheesecake-maracuya', 'Mini', 16.00, 8.00, 190, 25, 2, 'SKU-CHM-MIN-01', '7750000000022'
  UNION ALL SELECT 'porcion-tres-leches', 'Porcion Individual', 12.00, 5.50, 170, 18, 10, 'SKU-3L-POR-01', '7750000000031'
  UNION ALL SELECT 'porcion-red-velvet', 'Porcion Individual', 13.50, 6.20, 170, 18, 8, 'SKU-RV-POR-01', '7750000000041'
  UNION ALL SELECT 'brownie-clasico', 'Caja x6', 38.00, 19.00, 600, 45, 5, 'SKU-BR-X6-01', '7750000000051'
  UNION ALL SELECT 'tarta-de-frutas', 'Mediana', 72.00, 36.00, 1200, 140, 3, 'SKU-TF-MED-01', '7750000000061'
) s
WHERE NOT EXISTS (
  SELECT 1 FROM producto_variante v WHERE v.codigo_sku = s.codigo_sku
);

/* 6) RELACION PRODUCTO-ALERGENO */
INSERT INTO producto_alergeno (producto_id, alergeno_id)
SELECT p.id, a.id
FROM producto p
JOIN alergeno a ON a.nombre IN ('Gluten', 'Lactosa', 'Huevo')
WHERE p.slug IN ('selva-negra-clasica', 'cheesecake-maracuya', 'porcion-tres-leches', 'porcion-red-velvet', 'brownie-clasico', 'tarta-de-frutas')
  AND NOT EXISTS (
    SELECT 1 FROM producto_alergeno pa
    WHERE pa.producto_id = p.id
      AND pa.alergeno_id = a.id
  );

/* 7) PROMOCIONES */
INSERT INTO promocion (
  nombre, tipo_descuento, valor_descuento, monto_minimo, aplica_a,
  fecha_inicio, fecha_fin, activo, codigo_cupon, limite_usos_total, limite_usos_por_usuario
)
SELECT s.nombre, s.tipo_descuento, s.valor_descuento, s.monto_minimo, s.aplica_a,
       s.fecha_inicio, s.fecha_fin, s.activo, s.codigo_cupon, s.limite_usos_total, s.limite_usos_por_usuario
FROM (
  SELECT
    'Campana Otono 2026' AS nombre, 'PORCENTAJE' AS tipo_descuento, 12.00 AS valor_descuento, 80.00 AS monto_minimo, 'CARRITO' AS aplica_a,
    DATE_SUB(NOW(), INTERVAL 5 DAY) AS fecha_inicio, DATE_ADD(NOW(), INTERVAL 25 DAY) AS fecha_fin, 1 AS activo,
    'OTONO12' AS codigo_cupon, 500 AS limite_usos_total, 2 AS limite_usos_por_usuario
  UNION ALL
  SELECT
    'Envio Cero Lima' AS nombre, 'ENVIO_GRATIS' AS tipo_descuento, 0.00 AS valor_descuento, 60.00 AS monto_minimo, 'CARRITO' AS aplica_a,
    DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 20 DAY), 1,
    'ENVIO0LIM', 300, 3
) s
WHERE NOT EXISTS (
  SELECT 1 FROM promocion p WHERE p.codigo_cupon = s.codigo_cupon
);

/* 8) RELACION VARIANTE-PROMOCION */
INSERT INTO producto_promocion (variante_id, promocion_id)
SELECT v.id, p.id
FROM producto_variante v
JOIN promocion p ON p.codigo_cupon = 'OTONO12'
WHERE v.codigo_sku IN ('SKU-SN-ENT-01', 'SKU-CHM-ENT-01', 'SKU-TF-MED-01')
  AND NOT EXISTS (
    SELECT 1 FROM producto_promocion pp
    WHERE pp.variante_id = v.id
      AND pp.promocion_id = p.id
  );

/* 9) DIRECCIONES (cliente demo) */
INSERT INTO direccion (
  usuario_id, zona_id, etiqueta, direccion_completa, referencia,
  destinatario_nombre, destinatario_telefono, activo, es_principal, latitud, longitud
)
SELECT
  @cliente_id,
  (SELECT id FROM zona_envio WHERE nombre_distrito = 'Miraflores' LIMIT 1),
  'Casa', 'Av. Pardo 123, Miraflores', 'Edificio verde, depto 402',
  'Cliente Prueba', '987654321', 1, 1, -12.1212, -77.0297
WHERE @cliente_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM direccion d
    WHERE d.usuario_id = @cliente_id
      AND d.etiqueta = 'Casa'
      AND d.direccion_completa = 'Av. Pardo 123, Miraflores'
  );

INSERT INTO direccion (
  usuario_id, zona_id, etiqueta, direccion_completa, referencia,
  destinatario_nombre, destinatario_telefono, activo, es_principal, latitud, longitud
)
SELECT
  @cliente_id,
  (SELECT id FROM zona_envio WHERE nombre_distrito = 'Surco' LIMIT 1),
  'Oficina', 'Jr. Empresarial 890, Surco', 'Torre A, piso 5',
  'Cliente Prueba', '987654321', 1, 0, -12.1370, -76.9913
WHERE @cliente_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM direccion d
    WHERE d.usuario_id = @cliente_id
      AND d.etiqueta = 'Oficina'
      AND d.direccion_completa = 'Jr. Empresarial 890, Surco'
  );

SET @dir_id := (
  SELECT id FROM direccion
  WHERE usuario_id = @cliente_id AND activo = 1
  ORDER BY es_principal DESC, id ASC
  LIMIT 1
);

SET @franja_id := (
  SELECT id FROM franja_horaria
  WHERE fecha = DATE_ADD(CURDATE(), INTERVAL 1 DAY)
  ORDER BY hora_inicio ASC
  LIMIT 1
);

SET @promo_oton := (SELECT id FROM promocion WHERE codigo_cupon = 'OTONO12' LIMIT 1);

/* 10) PEDIDOS DEMO */
INSERT INTO pedido (
  usuario_id, direccion_id, franja_horaria_id, promocion_id,
  subtotal, descuento, costo_envio, impuestos, total, es_recojo_tienda,
  fecha_creacion, codigo_pedido, estado_actual_id, fecha_actualizacion
)
SELECT
  @cliente_id, @dir_id, @franja_id, @promo_oton,
  108.00, 12.00, 8.50, 19.44, 123.94, 0,
  DATE_SUB(NOW(), INTERVAL 3 DAY), 'PED-DEMO-1001', 6, DATE_SUB(NOW(), INTERVAL 2 DAY)
WHERE @cliente_id IS NOT NULL
  AND @dir_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM pedido p WHERE p.codigo_pedido = 'PED-DEMO-1001');

INSERT INTO pedido (
  usuario_id, direccion_id, franja_horaria_id, promocion_id,
  subtotal, descuento, costo_envio, impuestos, total, es_recojo_tienda,
  fecha_creacion, codigo_pedido, estado_actual_id, fecha_actualizacion
)
SELECT
  @cliente_id, @dir_id, @franja_id, NULL,
  76.00, 0.00, 9.50, 13.68, 99.18, 0,
  DATE_SUB(NOW(), INTERVAL 2 DAY), 'PED-DEMO-1002', 5, DATE_SUB(NOW(), INTERVAL 1 DAY)
WHERE @cliente_id IS NOT NULL
  AND @dir_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM pedido p WHERE p.codigo_pedido = 'PED-DEMO-1002');

INSERT INTO pedido (
  usuario_id, direccion_id, franja_horaria_id, promocion_id,
  subtotal, descuento, costo_envio, impuestos, total, es_recojo_tienda,
  fecha_creacion, codigo_pedido, estado_actual_id, fecha_actualizacion
)
SELECT
  @cliente_id, @dir_id, @franja_id, NULL,
  52.00, 0.00, 0.00, 9.36, 61.36, 1,
  DATE_SUB(NOW(), INTERVAL 1 DAY), 'PED-DEMO-1003', 2, DATE_SUB(NOW(), INTERVAL 1 DAY)
WHERE @cliente_id IS NOT NULL
  AND @dir_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM pedido p WHERE p.codigo_pedido = 'PED-DEMO-1003');

SET @ped1 := (SELECT id FROM pedido WHERE codigo_pedido = 'PED-DEMO-1001' LIMIT 1);
SET @ped2 := (SELECT id FROM pedido WHERE codigo_pedido = 'PED-DEMO-1002' LIMIT 1);
SET @ped3 := (SELECT id FROM pedido WHERE codigo_pedido = 'PED-DEMO-1003' LIMIT 1);

SET @v_selva := (SELECT id FROM producto_variante WHERE codigo_sku = 'SKU-SN-ENT-01' LIMIT 1);
SET @v_tresl := (SELECT id FROM producto_variante WHERE codigo_sku = 'SKU-3L-POR-01' LIMIT 1);
SET @v_chees := (SELECT id FROM producto_variante WHERE codigo_sku = 'SKU-CHM-ENT-01' LIMIT 1);
SET @v_brown := (SELECT id FROM producto_variante WHERE codigo_sku = 'SKU-BR-X6-01' LIMIT 1);
SET @v_redvl := (SELECT id FROM producto_variante WHERE codigo_sku = 'SKU-RV-POR-01' LIMIT 1);

/* 11) DETALLE PEDIDO */
INSERT INTO detalle_pedido (pedido_id, variante_id, nombre_snapshot, precio_unitario_snapshot, cantidad, subtotal_linea)
SELECT @ped1, @v_selva, 'Selva Negra Clasica - Entera 20cm', 95.00, 1, 95.00
WHERE @ped1 IS NOT NULL AND @v_selva IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM detalle_pedido d WHERE d.pedido_id = @ped1 AND d.variante_id = @v_selva);

INSERT INTO detalle_pedido (pedido_id, variante_id, nombre_snapshot, precio_unitario_snapshot, cantidad, subtotal_linea)
SELECT @ped1, @v_tresl, 'Porcion Tres Leches - Porcion Individual', 12.00, 1, 12.00
WHERE @ped1 IS NOT NULL AND @v_tresl IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM detalle_pedido d WHERE d.pedido_id = @ped1 AND d.variante_id = @v_tresl);

INSERT INTO detalle_pedido (pedido_id, variante_id, nombre_snapshot, precio_unitario_snapshot, cantidad, subtotal_linea)
SELECT @ped2, @v_chees, 'Cheesecake de Maracuya - Entera 18cm', 88.00, 1, 88.00
WHERE @ped2 IS NOT NULL AND @v_chees IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM detalle_pedido d WHERE d.pedido_id = @ped2 AND d.variante_id = @v_chees);

INSERT INTO detalle_pedido (pedido_id, variante_id, nombre_snapshot, precio_unitario_snapshot, cantidad, subtotal_linea)
SELECT @ped3, @v_brown, 'Brownie Clasico - Caja x6', 38.00, 1, 38.00
WHERE @ped3 IS NOT NULL AND @v_brown IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM detalle_pedido d WHERE d.pedido_id = @ped3 AND d.variante_id = @v_brown);

INSERT INTO detalle_pedido (pedido_id, variante_id, nombre_snapshot, precio_unitario_snapshot, cantidad, subtotal_linea)
SELECT @ped3, @v_redvl, 'Porcion Red Velvet - Porcion Individual', 13.50, 1, 13.50
WHERE @ped3 IS NOT NULL AND @v_redvl IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM detalle_pedido d WHERE d.pedido_id = @ped3 AND d.variante_id = @v_redvl);

/* 12) HISTORIAL ESTADOS */
INSERT INTO estado_pedido_historial (pedido_id, estado_id, usuario_id, observacion, fecha)
SELECT @ped1, 1, @cliente_id, 'Pedido creado', DATE_SUB(NOW(), INTERVAL 3 DAY)
WHERE @ped1 IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM estado_pedido_historial h WHERE h.pedido_id = @ped1 AND h.estado_id = 1);

INSERT INTO estado_pedido_historial (pedido_id, estado_id, usuario_id, observacion, fecha)
SELECT @ped1, 2, @admin_id, 'Pago confirmado por admin', DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 30 MINUTE
WHERE @ped1 IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM estado_pedido_historial h WHERE h.pedido_id = @ped1 AND h.estado_id = 2);

INSERT INTO estado_pedido_historial (pedido_id, estado_id, usuario_id, observacion, fecha)
SELECT @ped1, 6, @admin_id, 'Pedido entregado', DATE_SUB(NOW(), INTERVAL 2 DAY)
WHERE @ped1 IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM estado_pedido_historial h WHERE h.pedido_id = @ped1 AND h.estado_id = 6);

INSERT INTO estado_pedido_historial (pedido_id, estado_id, usuario_id, observacion, fecha)
SELECT @ped2, 1, @cliente_id, 'Pedido creado', DATE_SUB(NOW(), INTERVAL 2 DAY)
WHERE @ped2 IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM estado_pedido_historial h WHERE h.pedido_id = @ped2 AND h.estado_id = 1);

INSERT INTO estado_pedido_historial (pedido_id, estado_id, usuario_id, observacion, fecha)
SELECT @ped2, 2, @admin_id, 'Pago confirmado', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 20 MINUTE
WHERE @ped2 IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM estado_pedido_historial h WHERE h.pedido_id = @ped2 AND h.estado_id = 2);

INSERT INTO estado_pedido_historial (pedido_id, estado_id, usuario_id, observacion, fecha)
SELECT @ped2, 5, @admin_id, 'Pedido en ruta', DATE_SUB(NOW(), INTERVAL 1 DAY)
WHERE @ped2 IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM estado_pedido_historial h WHERE h.pedido_id = @ped2 AND h.estado_id = 5);

INSERT INTO estado_pedido_historial (pedido_id, estado_id, usuario_id, observacion, fecha)
SELECT @ped3, 1, @cliente_id, 'Pedido creado', DATE_SUB(NOW(), INTERVAL 1 DAY)
WHERE @ped3 IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM estado_pedido_historial h WHERE h.pedido_id = @ped3 AND h.estado_id = 1);

INSERT INTO estado_pedido_historial (pedido_id, estado_id, usuario_id, observacion, fecha)
SELECT @ped3, 2, @admin_id, 'Pago confirmado', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 15 MINUTE
WHERE @ped3 IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM estado_pedido_historial h WHERE h.pedido_id = @ped3 AND h.estado_id = 2);

/* 13) PAGOS */
INSERT INTO pago (
  pedido_id, metodo_pago_id, monto, estado, referencia_externa,
  intentos, fecha, moneda, id_transaccion_externa, fecha_aprobacion
)
SELECT @ped1, 1, 123.94, 'APROBADO', 'Culqi demo PED-DEMO-1001', 1, DATE_SUB(NOW(), INTERVAL 3 DAY), 'PEN', 'TRX-DEMO-1001', DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 10 MINUTE
WHERE @ped1 IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM pago p WHERE p.pedido_id = @ped1);

INSERT INTO pago (
  pedido_id, metodo_pago_id, monto, estado, referencia_externa,
  intentos, fecha, moneda, id_transaccion_externa, fecha_aprobacion
)
SELECT @ped2, 2, 99.18, 'APROBADO', 'Yape demo PED-DEMO-1002', 1, DATE_SUB(NOW(), INTERVAL 2 DAY), 'PEN', 'TRX-DEMO-1002', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 8 MINUTE
WHERE @ped2 IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM pago p WHERE p.pedido_id = @ped2);

INSERT INTO pago (
  pedido_id, metodo_pago_id, monto, estado, referencia_externa,
  intentos, fecha, moneda, id_transaccion_externa, fecha_aprobacion
)
SELECT @ped3, 4, 61.36, 'PENDIENTE', 'Efectivo demo PED-DEMO-1003', 0, DATE_SUB(NOW(), INTERVAL 1 DAY), 'PEN', 'TRX-DEMO-1003', NULL
WHERE @ped3 IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM pago p WHERE p.pedido_id = @ped3);

/* 14) NOTIFICACIONES */
INSERT INTO notificacion (
  usuario_id, pedido_id, canal, asunto, mensaje, leida, intentos, fecha_envio, estado_envio, destino_canal
)
SELECT @cliente_id, @ped1, 'EMAIL', 'Pedido entregado', 'Tu pedido PED-DEMO-1001 fue entregado con exito.', 0, 1, DATE_SUB(NOW(), INTERVAL 2 DAY), 'ENVIADA', 'cliente@gmail.com'
WHERE @cliente_id IS NOT NULL AND @ped1 IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM notificacion n WHERE n.usuario_id = @cliente_id AND n.pedido_id = @ped1 AND n.asunto = 'Pedido entregado'
  );

INSERT INTO notificacion (
  usuario_id, pedido_id, canal, asunto, mensaje, leida, intentos, fecha_envio, estado_envio, destino_canal
)
SELECT @cliente_id, @ped2, 'EMAIL', 'Pedido en ruta', 'Tu pedido PED-DEMO-1002 va en camino.', 0, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), 'ENVIADA', 'cliente@gmail.com'
WHERE @cliente_id IS NOT NULL AND @ped2 IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM notificacion n WHERE n.usuario_id = @cliente_id AND n.pedido_id = @ped2 AND n.asunto = 'Pedido en ruta'
  );

/* 15) INVENTARIO MOVIMIENTOS */
INSERT INTO inventario_movimiento (variante_id, tipo, cantidad, stock_resultante, motivo, pedido_id, usuario_id, fecha)
SELECT @v_selva, 'SALIDA', 1, 5, 'Venta PED-DEMO-1001', @ped1, @admin_id, DATE_SUB(NOW(), INTERVAL 3 DAY)
WHERE @v_selva IS NOT NULL AND @ped1 IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM inventario_movimiento im
    WHERE im.variante_id = @v_selva AND im.pedido_id = @ped1 AND im.motivo = 'Venta PED-DEMO-1001'
  );

INSERT INTO inventario_movimiento (variante_id, tipo, cantidad, stock_resultante, motivo, pedido_id, usuario_id, fecha)
SELECT @v_chees, 'SALIDA', 1, 3, 'Venta PED-DEMO-1002', @ped2, @admin_id, DATE_SUB(NOW(), INTERVAL 2 DAY)
WHERE @v_chees IS NOT NULL AND @ped2 IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM inventario_movimiento im
    WHERE im.variante_id = @v_chees AND im.pedido_id = @ped2 AND im.motivo = 'Venta PED-DEMO-1002'
  );

/* 16) REPORTE VENTA DIARIA */
INSERT INTO reporte_venta_diaria (
  fecha, total_pedidos, pedidos_entregados, ingresos_brutos, total_descuentos,
  ingresos_netos, ticket_promedio, metodo_pago_top, producto_top
)
SELECT
  x.fecha,
  x.total_pedidos,
  x.pedidos_entregados,
  x.ingresos_brutos,
  x.total_descuentos,
  x.ingresos_netos,
  x.ticket_promedio,
  'Tarjeta de credito/debito' AS metodo_pago_top,
  'Selva Negra Clasica' AS producto_top
FROM (
  SELECT
    DATE(p.fecha_creacion) AS fecha,
    COUNT(*) AS total_pedidos,
    SUM(CASE WHEN p.estado_actual_id = 6 THEN 1 ELSE 0 END) AS pedidos_entregados,
    SUM(p.total) AS ingresos_brutos,
    SUM(p.descuento) AS total_descuentos,
    SUM(p.total - p.descuento) AS ingresos_netos,
    AVG(p.total) AS ticket_promedio
  FROM pedido p
  WHERE p.codigo_pedido LIKE 'PED-DEMO-%'
  GROUP BY DATE(p.fecha_creacion)
) x
ON DUPLICATE KEY UPDATE
  total_pedidos = VALUES(total_pedidos),
  pedidos_entregados = VALUES(pedidos_entregados),
  ingresos_brutos = VALUES(ingresos_brutos),
  total_descuentos = VALUES(total_descuentos),
  ingresos_netos = VALUES(ingresos_netos),
  ticket_promedio = VALUES(ticket_promedio),
  metodo_pago_top = VALUES(metodo_pago_top),
  producto_top = VALUES(producto_top);

COMMIT;
