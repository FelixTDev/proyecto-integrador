package pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PagoRequestDTO(
        @NotNull(message = "El carritoId es requerido")
        Integer carritoId,

        @NotBlank(message = "El token de tarjeta es requerido")
        String tokenTarjeta,

        Integer direccionId,

        Integer franjaHorariaId,

        Boolean esRecojoTienda,

        String zonaEntrega,

        Boolean esUrgente,

        String codigoCupon,

        String email,

        String idempotencyKey,

        @NotNull(message = "El metodo de pago es requerido")
        Integer metodoPagoId
) {}