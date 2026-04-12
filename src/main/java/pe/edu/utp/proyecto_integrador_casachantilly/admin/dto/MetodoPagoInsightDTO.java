package pe.edu.utp.proyecto_integrador_casachantilly.admin.dto;

import java.math.BigDecimal;

public record MetodoPagoInsightDTO(
        String metodo,
        Long totalTransacciones,
        BigDecimal montoTotal,
        BigDecimal porcentajeUso
) {
}