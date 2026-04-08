package pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ComprobantePedidoDTO(
        Integer pedidoId,
        String codigoPedido,
        Integer usuarioId,
        Integer direccionId,
        Boolean recojoEnTienda,
        String estadoActual,
        LocalDateTime fechaCreacion,
        BigDecimal subtotal,
        BigDecimal descuento,
        BigDecimal costoEnvio,
        BigDecimal impuestos,
        BigDecimal total,
        List<ComprobanteItemDTO> items
) {}
