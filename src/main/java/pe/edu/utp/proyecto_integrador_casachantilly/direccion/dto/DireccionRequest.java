package pe.edu.utp.proyecto_integrador_casachantilly.direccion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DireccionRequest(
        @Size(max = 80, message = "La etiqueta no debe exceder 80 caracteres")
        String etiqueta,

        Integer zonaId,

        @NotBlank(message = "La dirección completa es obligatoria")
        String direccionCompleta,

        String referencia,

        @Size(max = 150, message = "El nombre del destinatario no debe exceder 150 caracteres")
        String destinatarioNombre,

        @Size(max = 25, message = "El teléfono del destinatario no debe exceder 25 caracteres")
        String destinatarioTelefono,

        Boolean esPrincipal
) {}
