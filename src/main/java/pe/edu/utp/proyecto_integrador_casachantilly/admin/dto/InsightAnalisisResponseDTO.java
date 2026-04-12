package pe.edu.utp.proyecto_integrador_casachantilly.admin.dto;

import java.util.List;

public record InsightAnalisisResponseDTO(
        List<TopProductoDTO> topProductos,
        List<HoraPicoDTO> horasPico,
        List<RotacionCategoriaDTO> rotacionCategorias,
        Integer rangoDias
) {
}