ALTER TABLE usuario
  ADD COLUMN fecha_actualizacion DATETIME NULL,
  ADD COLUMN fecha_desactivacion DATETIME NULL,
  ADD COLUMN fecha_verificacion_email DATETIME NULL,
  ADD COLUMN intentos_fallidos_login INT NOT NULL DEFAULT 0,
  ADD COLUMN bloqueado_hasta DATETIME NULL;

ALTER TABLE sesion
  ADD COLUMN agente_usuario VARCHAR(255) NULL,
  ADD COLUMN fecha_revocacion DATETIME NULL,
  ADD COLUMN motivo_revocacion VARCHAR(120) NULL;

ALTER TABLE pedido
  ADD COLUMN codigo_pedido VARCHAR(30) NULL,
  ADD COLUMN estado_actual_id INT NULL,
  ADD COLUMN fecha_actualizacion DATETIME NULL;

ALTER TABLE direccion
  ADD COLUMN es_principal TINYINT(1) NOT NULL DEFAULT 0,
  ADD COLUMN latitud DECIMAL(10,8) NULL,
  ADD COLUMN longitud DECIMAL(11,8) NULL;

ALTER TABLE producto
  ADD COLUMN fecha_actualizacion DATETIME NULL,
  ADD COLUMN slug VARCHAR(160) NULL;

ALTER TABLE producto_variante
  ADD COLUMN codigo_sku VARCHAR(60) NULL,
  ADD COLUMN codigo_barras VARCHAR(32) NULL;

ALTER TABLE promocion
  ADD COLUMN codigo_cupon VARCHAR(40) NULL,
  ADD COLUMN limite_usos_total INT NULL,
  ADD COLUMN limite_usos_por_usuario INT NULL;

ALTER TABLE pago
  ADD COLUMN moneda CHAR(3) NOT NULL DEFAULT 'PEN',
  ADD COLUMN id_transaccion_externa VARCHAR(120) NULL,
  ADD COLUMN fecha_aprobacion DATETIME NULL;

ALTER TABLE notificacion
  ADD COLUMN estado_envio ENUM('PENDIENTE','ENVIADA','ERROR','CANCELADA') NOT NULL DEFAULT 'PENDIENTE',
  ADD COLUMN destino_canal VARCHAR(160) NULL;

ALTER TABLE auditoria
  ADD COLUMN id_correlacion VARCHAR(64) NULL,
  ADD COLUMN modulo VARCHAR(80) NULL;

ALTER TABLE reclamo
  ADD COLUMN fecha_actualizacion DATETIME NULL,
  ADD COLUMN prioridad ENUM('BAJA','MEDIA','ALTA') NOT NULL DEFAULT 'MEDIA',
  ADD COLUMN detalle_resolucion TEXT NULL;

ALTER TABLE puntos_fidelidad
  ADD COLUMN saldo_resultante INT NULL;

CREATE UNIQUE INDEX ux_pedido_codigo_pedido ON pedido (codigo_pedido);
CREATE UNIQUE INDEX ux_producto_slug ON producto (slug);
CREATE UNIQUE INDEX ux_producto_variante_codigo_sku ON producto_variante (codigo_sku);
CREATE UNIQUE INDEX ux_promocion_codigo_cupon ON promocion (codigo_cupon);
CREATE UNIQUE INDEX ux_pago_id_transaccion_externa ON pago (id_transaccion_externa);

ALTER TABLE pedido
  ADD CONSTRAINT fk_ped_estado_actual
  FOREIGN KEY (estado_actual_id) REFERENCES estado_pedido(id);
