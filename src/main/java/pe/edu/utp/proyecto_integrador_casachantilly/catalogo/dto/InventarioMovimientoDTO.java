package pe.edu.utp.proyecto_integrador_casachantilly.catalogo.dto;

import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.entidad.TipoMovimiento;

import java.time.LocalDateTime;

public record InventarioMovimientoDTO(
        Integer id,
        Integer varianteId,
        TipoMovimiento tipo,
        Integer cantidad,
        Integer stockResultante,
        String motivo,
        Integer pedidoId,
        Integer usuarioId,
        LocalDateTime fecha
) {
}
