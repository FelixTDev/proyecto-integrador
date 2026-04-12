package pe.edu.utp.proyecto_integrador_casachantilly.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Comparator;
import java.util.UUID;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generarToken(String email, Collection<? extends GrantedAuthority> authorities) {
        List<String> rolesConPrefijo = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .toList();
        List<String> roles = rolesConPrefijo.stream()
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .map(String::toUpperCase)
                .distinct()
                .sorted(Comparator.comparingInt(this::rolePriority))
                .toList();
        String role = roles.isEmpty() ? "CLIENTE" : roles.get(0);

        return Jwts.builder()
                .subject(email)
                .id(UUID.randomUUID().toString())
                .claim("roles", roles)
                .claim("authorities", rolesConPrefijo)
                .claim("role", role)
                .claim("rol", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSignKey())
                .compact();
    }

    private int rolePriority(String role) {
        return switch (role) {
            case "ADMIN" -> 0;
            case "VENDEDOR" -> 1;
            case "CLIENTE" -> 2;
            default -> 9;
        };
    }

    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
