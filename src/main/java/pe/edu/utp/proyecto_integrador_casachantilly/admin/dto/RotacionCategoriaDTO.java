package pe.edu.utp.proyecto_integrador_casachantilly.admin.dto;

import java.math.BigDecimal;

public record RotacionCategoriaDTO(
        String categoria,
        Long unidades,
        BigDecimal porcentaje
) {
}