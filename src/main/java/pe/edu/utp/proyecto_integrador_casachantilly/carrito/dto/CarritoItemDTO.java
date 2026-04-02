package pe.edu.utp.proyecto_integrador_casachantilly.carrito.dto;

import java.math.BigDecimal;

public record CarritoItemDTO(
        Integer detalleId,
        Integer varianteId,
        String productoNombre,
        String varianteNombre,
        BigDecimal precioUnitario,
        Integer cantidad,
        BigDecimal subtotal,
        Integer stockDisponible
) {}
