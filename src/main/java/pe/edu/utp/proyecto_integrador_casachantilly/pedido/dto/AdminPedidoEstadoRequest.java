package pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto;

import jakarta.validation.constraints.NotNull;

public record AdminPedidoEstadoRequest(
        @NotNull(message = "estadoId es requerido")
        Integer estadoId,
        String observacion
) {}
