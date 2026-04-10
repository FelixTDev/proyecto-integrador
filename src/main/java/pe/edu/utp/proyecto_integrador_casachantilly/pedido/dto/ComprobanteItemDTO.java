package pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto;

import java.math.BigDecimal;

public record ComprobanteItemDTO(
        Integer detalleId,
        Integer varianteId,
        String nombre,
        BigDecimal precioUnitario,
        Integer cantidad,
        BigDecimal subtotal
) {}
