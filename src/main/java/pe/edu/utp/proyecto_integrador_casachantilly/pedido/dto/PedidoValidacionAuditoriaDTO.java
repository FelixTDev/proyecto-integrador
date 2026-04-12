package pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto;

import java.time.LocalDateTime;

public record PedidoValidacionAuditoriaDTO(
        Integer id,
        Integer pedidoId,
        Integer usuarioId,
        String usuarioNombre,
        String resultado,
        String motivo,
        LocalDateTime fecha
) {
}
