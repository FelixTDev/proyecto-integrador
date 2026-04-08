package pe.edu.utp.proyecto_integrador_casachantilly.catalogo.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductoCardDTO(
        Integer id,
        String nombre,
        String descripcion,
        BigDecimal precioMinimo,
        String urlFotoPortada,
        String nombreCategoria,
        List<String> listaAlergenos,
        boolean hayStock,
        Boolean activo
) {}
