package pe.edu.utp.proyecto_integrador_casachantilly.notificacion.dto;

import java.time.LocalDateTime;

public record NotificacionDTO(
        Integer id,
        Integer usuarioId,
        Integer pedidoId,
        String canal,
        String asunto,
        String mensaje,
        Boolean leida,
        Integer intentos,
        String estadoEnvio,
        String destinoCanal,
        LocalDateTime fechaEnvio
) {}
