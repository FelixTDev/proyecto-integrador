package pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto;

import java.math.BigDecimal;

public record ReordenarItemResultadoDTO(
        Integer varianteId,
        String descripcion,
        Integer cantidadSolicitada,
        Integer cantidadAgregada,
        BigDecimal precioAnterior,
        BigDecimal precioActual,
        String estado,
        String mensaje
) {}
