package pe.edu.utp.proyecto_integrador_casachantilly.reclamo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReclamoRequest(
    @NotNull Integer pedidoId,
    @NotBlank String tipo,
    @NotBlank String descripcion
) {}
