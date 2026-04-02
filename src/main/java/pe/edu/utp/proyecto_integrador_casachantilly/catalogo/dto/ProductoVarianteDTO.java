package pe.edu.utp.proyecto_integrador_casachantilly.catalogo.dto;

import java.math.BigDecimal;

public record ProductoVarianteDTO(
        Integer id,
        String nombreVariante,
        BigDecimal precio,
        BigDecimal costo,
        Integer pesoGramos,
        Integer tiempoPrepMin,
        Integer stockDisponible,
        Boolean activo
) {}
