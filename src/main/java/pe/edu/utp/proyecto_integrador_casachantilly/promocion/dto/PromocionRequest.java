package pe.edu.utp.proyecto_integrador_casachantilly.promocion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PromocionRequest(
    @NotBlank String nombre,
    @NotBlank String tipoDescuento,
    @NotNull BigDecimal valorDescuento,
    BigDecimal montoMinimo,
    String aplicaA,
    LocalDateTime fechaInicio,
    LocalDateTime fechaFin,
    Boolean activo,
    String codigoCupon,
    Integer limiteUsosTotal,
    Integer limiteUsosPorUsuario
) {}
