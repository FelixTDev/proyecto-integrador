package pe.edu.utp.proyecto_integrador_casachantilly.auth.dto;

import java.util.List;

public record AuthResponse(
        String token,
        String email,
        String tokenType,
        String tipo,
        String role,
        String rol,
        List<String> roles
) {
    public static AuthResponse of(String token, String email) {
        return of(token, email, List.of());
    }

    public static AuthResponse of(String token, String email, List<String> roles) {
        List<String> safeRoles = roles == null ? List.of() : roles;
        String primaryRole = safeRoles.isEmpty() ? "CLIENTE" : safeRoles.get(0);
        return new AuthResponse(token, email, "Bearer", primaryRole, primaryRole, primaryRole, safeRoles);
    }
}
