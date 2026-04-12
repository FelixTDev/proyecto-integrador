package pe.edu.utp.proyecto_integrador_casachantilly.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PromocionInsightDTO(
        Integer id,
        String nombre,
        String tipoDescuento,
        BigDecimal valorDescuento,
        BigDecimal montoMinimo,
        String aplicaA,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        Boolean activo,
        String codigoCupon,
        String estado
) {
}