package pe.edu.utp.proyecto_integrador_casachantilly.catalogo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.entidad.TipoMovimiento;

public record InventarioMovimientoRequest(

        @NotNull(message = "El tipo de movimiento es requerido")
        TipoMovimiento tipo,

        @NotNull(message = "La cantidad es requerida")
        @Min(value = 1, message = "La cantidad debe ser mayor a 0")
        Integer cantidad,

        String motivo
) {}
