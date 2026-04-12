package pe.edu.utp.proyecto_integrador_casachantilly.admin.dto;

import java.util.List;

public record InsightReporteResponseDTO(
        InsightKpiDTO kpis,
        List<VentaDiariaDTO> ventasDiarias,
        TipoEntregaDTO tiposEntrega,
        List<MetodoPagoInsightDTO> metodosPago,
        Integer rangoDias
) {
}