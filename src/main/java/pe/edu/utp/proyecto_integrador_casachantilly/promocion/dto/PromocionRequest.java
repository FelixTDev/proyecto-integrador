package pe.edu.utp.proyecto_integrador_casachantilly.promocion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PromocionRequest(
    @NotBlank String nombre,
    @NotBlank
    @Pattern(regexp = "(?i)^(PORCENTAJE|MONTO_FIJO|2X1|ENVIO_GRATIS)$",
            message = "tipoDescuento invalido. Valores: PORCENTAJE, MONTO_FIJO, 2X1, ENVIO_GRATIS")
    String tipoDescuento,
    @NotNull BigDecimal valorDescuento,
    BigDecimal montoMinimo,
    @Pattern(regexp = "(?i)^(PRODUCTO|CARRITO)?$",
            message = "aplicaA invalido. Valores: PRODUCTO o CARRITO")
    String aplicaA,
    LocalDateTime fechaInicio,
    LocalDateTime fechaFin,
    Boolean activo,
    String codigoCupon,
    Integer limiteUsosTotal,
    Integer limiteUsosPorUsuario
) {}
