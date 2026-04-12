package pe.edu.utp.proyecto_integrador_casachantilly.admin.dto;

import java.math.BigDecimal;

public record TipoEntregaDTO(
        BigDecimal deliveryPct,
        BigDecimal recojoPct,
        Long deliveryCount,
        Long recojoCount
) {
}