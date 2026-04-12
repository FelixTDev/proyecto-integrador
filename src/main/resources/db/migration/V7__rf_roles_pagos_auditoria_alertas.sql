SET NAMES utf8mb4;

INSERT INTO rol (nombre)
SELECT 'VENDEDOR'
WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'VENDEDOR');

CREATE TABLE IF NOT EXISTS pedido_validacion_auditoria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    pedido_id INT NOT NULL,
    usuario_id INT NOT NULL,
    resultado VARCHAR(20) NOT NULL,
    motivo VARCHAR(255) NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_pedido_validacion_pedido_fecha (pedido_id, fecha),
    INDEX idx_pedido_validacion_usuario (usuario_id),
    CONSTRAINT fk_pedido_validacion_pedido FOREIGN KEY (pedido_id) REFERENCES pedido(id),
    CONSTRAINT fk_pedido_validacion_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

ALTER TABLE pago
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(80) NULL,
    ADD COLUMN IF NOT EXISTS codigo_error_proveedor VARCHAR(80) NULL;

ALTER TABLE pago
    ADD UNIQUE KEY uk_pago_idempotency_key (idempotency_key);

ALTER TABLE notificacion
    ADD COLUMN IF NOT EXISTS error_proveedor VARCHAR(255) NULL;