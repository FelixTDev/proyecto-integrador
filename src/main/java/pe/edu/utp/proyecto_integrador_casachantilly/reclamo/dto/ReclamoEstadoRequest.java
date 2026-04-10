package pe.edu.utp.proyecto_integrador_casachantilly.reclamo.dto;

import java.math.BigDecimal;

public record ReclamoEstadoRequest(
    String estado, String detalleResolucion, BigDecimal montoReembolso
) {}
