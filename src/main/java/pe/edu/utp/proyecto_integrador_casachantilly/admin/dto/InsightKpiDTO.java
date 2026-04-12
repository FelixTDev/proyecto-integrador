package pe.edu.utp.proyecto_integrador_casachantilly.admin.dto;

import java.math.BigDecimal;

public record InsightKpiDTO(
        BigDecimal ventasBrutas,
        Long pedidosCompletados,
        BigDecimal ticketPromedio,
        BigDecimal cancelacionesPct,
        Long pedidosTotales
) {
}