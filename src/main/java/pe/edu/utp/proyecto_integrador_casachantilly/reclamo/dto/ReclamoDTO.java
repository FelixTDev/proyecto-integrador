package pe.edu.utp.proyecto_integrador_casachantilly.reclamo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReclamoDTO(
    Integer id, Integer pedidoId, String codigoPedido,
    Integer usuarioId, String nombreCliente,
    String tipo, String descripcion, String estado,
    BigDecimal montoReembolso, String prioridad,
    LocalDateTime fechaCreacion, LocalDateTime fechaResolucion,
    String detalleResolucion
) {}
