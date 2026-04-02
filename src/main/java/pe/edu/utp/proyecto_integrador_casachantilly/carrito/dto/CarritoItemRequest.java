package pe.edu.utp.proyecto_integrador_casachantilly.carrito.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CarritoItemRequest(
        @NotNull(message = "El varianteId es requerido")
        Integer varianteId,

        @NotNull(message = "La cantidad es requerida")
        @Min(value = 1, message = "La cantidad mínima es 1")
        Integer cantidad
) {}
