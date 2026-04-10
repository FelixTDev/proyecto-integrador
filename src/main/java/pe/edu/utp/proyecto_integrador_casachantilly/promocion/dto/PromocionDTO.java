package pe.edu.utp.proyecto_integrador_casachantilly.promocion.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PromocionDTO(
    Integer id, String nombre, String tipoDescuento, BigDecimal valorDescuento,
    BigDecimal montoMinimo, String aplicaA, LocalDateTime fechaInicio,
    LocalDateTime fechaFin, Boolean activo, String codigoCupon,
    Integer limiteUsosTotal, Integer limiteUsosPorUsuario, String estado
) {}
