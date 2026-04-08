package pe.edu.utp.proyecto_integrador_casachantilly.direccion.dto;

import java.math.BigDecimal;

public record ZonaEnvioDTO(
        Integer id,
        String nombreDistrito,
        BigDecimal costoDelivery,
        Integer tiempoEstimadoMin
) {}
