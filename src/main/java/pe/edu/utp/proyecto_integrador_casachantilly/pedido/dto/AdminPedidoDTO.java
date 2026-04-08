package pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminPedidoDTO(
        Integer id,
        String codigoPedido,
        Integer usuarioId,
        String clienteNombre,
        String clienteEmail,
        BigDecimal total,
        LocalDateTime fechaCreacion,
        Integer estadoId,
        String estado
) {}
