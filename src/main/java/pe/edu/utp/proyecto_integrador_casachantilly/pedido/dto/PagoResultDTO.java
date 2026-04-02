package pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto;

import java.math.BigDecimal;

public record PagoResultDTO(
        boolean aprobado,
        String mensaje,
        Integer pedidoId,
        Integer pagoId,
        String referenciaExterna,
        BigDecimal montoTotal,
        Integer intentos
) {}
