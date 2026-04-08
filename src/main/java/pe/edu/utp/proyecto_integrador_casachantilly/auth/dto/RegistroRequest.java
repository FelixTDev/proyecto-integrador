package pe.edu.utp.proyecto_integrador_casachantilly.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank(message = "El nombre es requerido")
        @Pattern(
                regexp = "^[\\p{L}\\s]+$",
                message = "El nombre solo debe contener letras y espacios"
        )
        String nombre,

        @NotBlank(message = "El email es requerido")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "La contraseña es requerida")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password,

        @Pattern(
                regexp = "^$|^\\d{9}$",
                message = "El número de celular debe tener 9 dígitos"
        )
        String telefono
) {}
