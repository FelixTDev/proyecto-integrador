package pe.edu.utp.proyecto_integrador_casachantilly.promocion.dto;

import java.math.BigDecimal;

public record CuponResultDTO(
    boolean aplicado, String mensaje, BigDecimal descuento,
    String tipoDescuento, String nombrePromocion
) {}
