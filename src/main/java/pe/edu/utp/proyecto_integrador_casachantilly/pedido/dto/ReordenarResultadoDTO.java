package pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto;

import java.util.List;

public record ReordenarResultadoDTO(
        Integer pedidoOrigenId,
        Integer carritoId,
        Integer totalItemsSolicitados,
        Integer totalItemsAgregados,
        Integer totalItemsRechazados,
        List<ReordenarItemResultadoDTO> items,
        String mensaje
) {}
