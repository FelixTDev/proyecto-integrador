package pe.edu.utp.proyecto_integrador_casachantilly.direccion.dto;

public record DireccionDTO(
        Integer id,
        Integer usuarioId,
        Integer zonaId,
        String zonaNombre,
        String etiqueta,
        String direccionCompleta,
        String referencia,
        String destinatarioNombre,
        String destinatarioTelefono,
        Boolean activo,
        Boolean esPrincipal
) {}
