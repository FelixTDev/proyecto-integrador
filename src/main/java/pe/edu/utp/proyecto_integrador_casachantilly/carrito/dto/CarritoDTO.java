package pe.edu.utp.proyecto_integrador_casachantilly.carrito.dto;

import java.math.BigDecimal;
import java.util.List;

public record CarritoDTO(
        Integer id,
        Integer usuarioId,
        List<CarritoItemDTO> items,
        BigDecimal subtotal,
        BigDecimal igv,
        BigDecimal total
) {}
