package pe.edu.utp.proyecto_integrador_casachantilly.auth.dto;

import java.time.LocalDateTime;

public record AdminEmpleadoListItemResponse(
        Integer id,
        String nombre,
        String email,
        String rol,
        Boolean activo,
        LocalDateTime fechaCreacion
) {}
