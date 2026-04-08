package pe.edu.utp.proyecto_integrador_casachantilly.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank(message = "El token es obligatorio")
    String token,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La nueva contraseña debe tener al menos 6 caracteres")
    String nuevaPassword
) {}
