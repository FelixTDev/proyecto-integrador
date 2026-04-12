package pe.edu.utp.proyecto_integrador_casachantilly.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VentaDiariaDTO(
        LocalDate fecha,
        Long totalPedidos,
        BigDecimal ingresosBrutos
) {
}