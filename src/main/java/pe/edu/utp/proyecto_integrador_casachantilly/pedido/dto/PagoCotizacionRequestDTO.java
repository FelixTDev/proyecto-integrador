package pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto;

import jakarta.validation.constraints.NotNull;

public record PagoCotizacionRequestDTO(
        @NotNull(message = "El carritoId es requerido")
        Integer carritoId,
        Integer direccionId,
        Boolean esRecojoTienda,
        Integer franjaHorariaId,
        String zonaEntrega
) {}
