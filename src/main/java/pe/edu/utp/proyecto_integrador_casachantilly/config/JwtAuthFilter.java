package pe.edu.utp.proyecto_integrador_casachantilly.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.servicio.AuthService;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.servicio.UsuarioDetailsService;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.dto.ApiErrorData;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.dto.ApiResponse;

import java.io.IOException;
import java.util.Set;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Set<String> PROTECTED_PREFIXES = Set.of(
            "/api/carrito", "/api/pedidos", "/api/pagos", "/api/cliente", "/api/admin");

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UsuarioDetailsService usuarioDetailsService;
    @Autowired
    private ObjectMapper objectMapper;
    @Lazy
    @Autowired
    private AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                if (!jwtUtil.isTokenValid(token)) {
                    if (isProtectedRequest(request)) {
                        writeUnauthorized(request, response, "Token invalido o expirado");
                        return;
                    }
                } else {
                    AuthService.SesionEstado estado = authService.validarYRenovarSesion(token);
                    if (estado == AuthService.SesionEstado.ACTIVA) {
                        String email = jwtUtil.extractEmail(token);
                        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                            UserDetails userDetails = usuarioDetailsService.loadUserByUsername(email);
                            UsernamePasswordAuthenticationToken authToken =
                                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                        }
                    } else if (isProtectedRequest(request)) {
                        String msg = switch (estado) {
                            case EXPIRADA -> "Sesion expirada por inactividad";
                            case REVOCADA -> "Sesion cerrada";
                            case NO_ENCONTRADA -> "Sesion no valida";
                            default -> "No autorizado";
                        };
                        writeUnauthorized(request, response, msg);
                        return;
                    }
                }
            } catch (Exception ex) {
                SecurityContextHolder.clearContext();
                if (isProtectedRequest(request)) {
                    writeUnauthorized(request, response, "Sesion invalida. Vuelve a iniciar sesion");
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isProtectedRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        return PROTECTED_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private void writeUnauthorized(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ApiResponse<ApiErrorData> body = new ApiResponse<>(
                false,
                message,
                ApiErrorData.of("UNAUTHORIZED", request.getRequestURI()));
        objectMapper.writeValue(response.getWriter(), body);
    }
}
