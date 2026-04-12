package pe.edu.utp.proyecto_integrador_casachantilly.admin.dto;

import java.math.BigDecimal;

public record HoraPicoDTO(
        String nombre,
        String rango,
        Long pedidos,
        BigDecimal porcentaje
) {
}