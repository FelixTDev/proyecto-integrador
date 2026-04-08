package pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto;

import jakarta.validation.constraints.NotNull;

public record AdminPedidoValidacionRequest(
        @NotNull(message = "Indique si el pedido se aprueba o no")
        Boolean aprobar,
        String motivo
) {}
