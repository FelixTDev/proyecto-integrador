package pe.edu.utp.proyecto_integrador_casachantilly.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SocialLoginRequest(
        @NotBlank(message = "El email es requerido")
        @Email(message = "Email invalido")
        String email,
        @NotBlank(message = "El nombre es requerido")
        String nombre,
        @NotBlank(message = "El oauthId es requerido")
        String oauthId
) {
}
