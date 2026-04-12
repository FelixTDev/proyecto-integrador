package pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto;

import java.math.BigDecimal;

public record PagoCotizacionDTO(
        Integer carritoId,
        BigDecimal subtotal,
        BigDecimal impuestos,
        BigDecimal descuento,
        BigDecimal costoEnvio,
        BigDecimal total,
        boolean franjaDisponible,
        String mensajeFranja,
        String codigoCupon,
        String tipoPromocion,
        boolean envioGratisAplicado,
        BigDecimal costoEnvioOriginal,
        boolean esUrgente,
        BigDecimal recargoUrgencia
) {}