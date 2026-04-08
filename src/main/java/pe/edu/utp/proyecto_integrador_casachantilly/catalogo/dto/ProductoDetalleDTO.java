package pe.edu.utp.proyecto_integrador_casachantilly.catalogo.dto;

import java.util.List;

public record ProductoDetalleDTO(
        Integer id,
        String nombre,
        String descripcion,
        Boolean activo,
        Integer categoriaId,
        String nombreCategoria,
        String imagenUrl,
        List<String> listaAlergenos,
        List<ProductoVarianteDTO> variantes
) {}
