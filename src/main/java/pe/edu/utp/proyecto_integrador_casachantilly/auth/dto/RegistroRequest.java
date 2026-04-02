package pe.edu.utp.proyecto_integrador_casachantilly.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank(message = "El nombre es requerido")
        String nombre,

        @NotBlank(message = "El email es requerido")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "La contraseña es requerida")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password,

        String telefono
) {}
