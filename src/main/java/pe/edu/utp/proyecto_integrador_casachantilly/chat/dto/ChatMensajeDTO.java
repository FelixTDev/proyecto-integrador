package pe.edu.utp.proyecto_integrador_casachantilly.chat.dto;

import java.time.LocalDateTime;

public record ChatMensajeDTO(
    Integer id, Integer pedidoId, Integer usuarioId,
    String nombreUsuario, String mensaje,
    Boolean leido, LocalDateTime fecha, boolean esMio
) {}
