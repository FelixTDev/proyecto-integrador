package pe.edu.utp.proyecto_integrador_casachantilly.auth.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.dto.AuthMeResponse;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.dto.AuthResponse;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.dto.LoginRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.dto.RecuperarPasswordRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.dto.RegistroRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.dto.ResetPasswordRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.dto.SocialLoginRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.servicio.AuthRateLimitService;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.servicio.AuthService;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.dto.ApiResponse;

@Tag(name = "Autenticacion", description = "Registro, login y logout con JWT")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private AuthRateLimitService authRateLimitService;

    @Operation(summary = "Registrar nuevo usuario (rol CLIENTE)")
    @PostMapping("/registro")
    public ResponseEntity<ApiResponse<AuthResponse>> registro(@Valid @RequestBody RegistroRequest request) {
        AuthResponse result = authService.registro(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Registro exitoso", result));
    }

    @Operation(summary = "Iniciar sesion y obtener token JWT")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        String emailNormalizado = request.email() == null ? "" : request.email().trim().toLowerCase();
        String key = emailNormalizado + "|" + ip;

        authRateLimitService.assertLoginAllowed(key);
        AuthResponse result;
        try {
            result = authService.login(request, ip, userAgent);
        } catch (RuntimeException ex) {
            authRateLimitService.recordLoginFailure(key);
            throw ex;
        }
        authRateLimitService.clearLoginFailures(key);
        return ResponseEntity.ok(ApiResponse.ok("Login exitoso", result));
    }

    @Operation(summary = "Login social con Google")
    @PostMapping("/social/google")
    public ResponseEntity<ApiResponse<AuthResponse>> loginGoogle(
            @Valid @RequestBody SocialLoginRequest request,
            HttpServletRequest httpRequest) {
        AuthResponse result = authService.loginSocial(request, "GOOGLE", httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"));
        return ResponseEntity.ok(ApiResponse.ok("Login social exitoso", result));
    }

    @Operation(summary = "Login social con Facebook")
    @PostMapping("/social/facebook")
    public ResponseEntity<ApiResponse<AuthResponse>> loginFacebook(
            @Valid @RequestBody SocialLoginRequest request,
            HttpServletRequest httpRequest) {
        AuthResponse result = authService.loginSocial(request, "FACEBOOK", httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"));
        return ResponseEntity.ok(ApiResponse.ok("Login social exitoso", result));
    }

    @Operation(summary = "Cerrar sesion (invalidar token)")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authService.logout(authHeader.substring(7));
        }
        return ResponseEntity.ok(ApiResponse.ok("Sesion cerrada", null));
    }

    @Operation(summary = "Refrescar token JWT y rotar sesion")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest httpRequest) {
        AuthResponse result = authService.refreshToken(authHeader, httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"));
        return ResponseEntity.ok(ApiResponse.ok("Token refrescado", result));
    }

    @Operation(summary = "Solicitar recuperacion de password")
    @PostMapping("/recuperar-password")
    public ResponseEntity<ApiResponse<Void>> recuperarPassword(
            @Valid @RequestBody RecuperarPasswordRequest request,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        String emailNormalizado = request.email() == null ? "" : request.email().trim().toLowerCase();
        authRateLimitService.consumeResetRequest("ip:" + ip);
        authRateLimitService.consumeResetRequest("email:" + emailNormalizado);

        authService.solicitarRecuperacionPassword(request.email());
        return ResponseEntity.ok(ApiResponse.ok("Si el correo existe, se enviaran instrucciones para recuperar la password.", null));
    }

    @Operation(summary = "Establecer nueva password usando token de recuperacion")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.nuevaPassword());
        return ResponseEntity.ok(ApiResponse.ok("Password actualizada exitosamente", null));
    }

    @Operation(summary = "Obtener datos del usuario logueado")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthMeResponse>> me(org.springframework.security.core.Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("No autenticado"));
        }
        AuthMeResponse resp = authService.obtenerPerfil(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok("OK", resp));
    }
}