package pe.edu.utp.proyecto_integrador_casachantilly.operacion.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record AlertaOperativaDTO(
        String tipo,
        String titulo,
        String mensaje,
        LocalDateTime fecha,
        Map<String, Object> metadata
) {
}
