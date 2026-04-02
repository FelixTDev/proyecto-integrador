package pe.edu.utp.proyecto_integrador_casachantilly.auth.dto;

public record AuthResponse(String token, String email, String tipo) {
    public static AuthResponse of(String token, String email) {
        return new AuthResponse(token, email, "Bearer");
    }
}
