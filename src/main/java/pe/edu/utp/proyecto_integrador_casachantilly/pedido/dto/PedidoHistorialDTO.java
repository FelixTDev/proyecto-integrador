package pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoHistorialDTO(
        Integer pedidoId,
        String codigoPedido,
        LocalDateTime fechaCreacion,
        Integer estadoId,
        String estado,
        BigDecimal total,
        Boolean recojoEnTienda,
        Integer direccionId,
        List<PedidoHistorialItemDTO> items
) {}
