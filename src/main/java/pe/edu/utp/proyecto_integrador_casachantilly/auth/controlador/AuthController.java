package pe.edu.utp.proyecto_integrador_casachantilly.auth.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.dto.AuthResponse;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.dto.LoginRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.dto.RecuperarPasswordRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.dto.RegistroRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.dto.ResetPasswordRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.servicio.AuthService;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.dto.ApiResponse;

@Tag(name = "Autenticación", description = "Registro, login y logout con JWT")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(summary = "Registrar nuevo usuario (rol CLIENTE)")
    @PostMapping("/registro")
    public ResponseEntity<ApiResponse<AuthResponse>> registro(@Valid @RequestBody RegistroRequest request) {
        AuthResponse result = authService.registro(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Registro exitoso", result));
    }

    @Operation(summary = "Iniciar sesión y obtener token JWT")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        String agenteUsuario = httpRequest.getHeader("User-Agent");
        AuthResponse result = authService.login(request, ip, agenteUsuario);
        return ResponseEntity.ok(ApiResponse.ok("Login exitoso", result));
    }

    @Operation(summary = "Cerrar sesión (invalidar token)")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authService.logout(authHeader.substring(7));
        }
        return ResponseEntity.ok(ApiResponse.ok("Sesión cerrada", null));
    }

    @Operation(summary = "Solicitar link de recuperación de contraseña")
    @PostMapping("/recuperar-password")
    public ResponseEntity<ApiResponse<Void>> recuperarPassword(@Valid @RequestBody RecuperarPasswordRequest request) {
        authService.solicitarRecuperacionPassword(request.email());
        return ResponseEntity.ok(ApiResponse.ok("Si el correo existe, se enviarán las instrucciones para recuperar la contraseña.", null));
    }

    @Operation(summary = "Establecer nueva contraseña usando el token de recuperación")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.nuevaPassword());
        return ResponseEntity.ok(ApiResponse.ok("Contraseña actualizada exitosamente", null));
    }
}
