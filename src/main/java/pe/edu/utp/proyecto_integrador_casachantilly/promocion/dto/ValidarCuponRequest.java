package pe.edu.utp.proyecto_integrador_casachantilly.promocion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ValidarCuponRequest(
        @NotBlank(message = "El codigo de cupon es requerido")
        String codigoCupon,
        @NotNull(message = "El subtotal es requerido")
        @DecimalMin(value = "0.0", inclusive = false, message = "El subtotal debe ser mayor a 0")
        BigDecimal subtotal,
        @DecimalMin(value = "0.0", inclusive = true, message = "El costo de envio no puede ser negativo")
        BigDecimal costoEnvio
) {
}
