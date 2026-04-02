package pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PagoRequestDTO(
        @NotNull(message = "El carritoId es requerido")
        Integer carritoId,

        /** Token generado por Culqi.js en el frontend */
        @NotBlank(message = "El token de tarjeta es requerido")
        String tokenTarjeta,

        Integer direccionId,

        Integer franjaHorariaId,

        /** Si es recojo en tienda (no necesita dirección) */
        Boolean esRecojoTienda,

        String email,

        /** Método de pago: 1=Tarjeta, 2=Yape, etc. */
        @NotNull(message = "El método de pago es requerido")
        Integer metodoPagoId
) {}
