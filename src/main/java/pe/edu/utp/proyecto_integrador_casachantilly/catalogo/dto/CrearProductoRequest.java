package pe.edu.utp.proyecto_integrador_casachantilly.catalogo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record CrearProductoRequest(

        @NotBlank(message = "El nombre es requerido")
        String nombre,

        String descripcion,

        @NotNull(message = "La categoría es requerida")
        Integer categoriaId,

        Boolean activo,

        List<Integer> alergenoIds,

        @NotEmpty(message = "Debe incluir al menos una variante")
        @Valid
        List<VarianteRequest> variantes

) {
    public record VarianteRequest(

            @NotBlank(message = "El nombre de variante es requerido")
            String nombreVariante,

            @NotNull(message = "El precio es requerido")
            @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
            BigDecimal precio,

            BigDecimal costo,

            Integer pesoGramos,

            Integer tiempoPrepMin,

            @Min(value = 0, message = "El stock no puede ser negativo")
            Integer stockDisponible,

            Boolean activo
    ) {}
}
