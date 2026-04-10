package pe.edu.utp.proyecto_integrador_casachantilly.chat.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatMensajeRequest(
    @NotBlank String mensaje
) {}
