package pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto;

import java.math.BigDecimal;

public record ComprobanteItemDTO(
        Integer varianteId,
        String descripcion,
        BigDecimal precioUnitario,
        Integer cantidad,
        BigDecimal subtotal
) {}
