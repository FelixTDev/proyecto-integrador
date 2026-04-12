package pe.edu.utp.proyecto_integrador_casachantilly.auth.dto;

import java.util.List;

public record AuthMeResponse(
        Integer id,
        String nombre,
        String email,
        String telefono,
        String role,
        String rol,
        List<String> roles
) {
}
