package pe.edu.utp.proyecto_integrador_casachantilly.fidelidad.dto;

import java.time.LocalDateTime;

public record PuntoMovimientoDTO(
    Integer id, Integer pedidoId, Integer puntos,
    String tipo, String descripcion, LocalDateTime fecha
) {}
