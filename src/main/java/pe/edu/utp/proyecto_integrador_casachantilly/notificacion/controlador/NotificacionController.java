package pe.edu.utp.proyecto_integrador_casachantilly.notificacion.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.entidad.Usuario;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.repositorio.UsuarioRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.dto.ApiResponse;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.ResourceNotFoundException;
import pe.edu.utp.proyecto_integrador_casachantilly.notificacion.dto.NotificacionDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.notificacion.servicio.NotificacionService;

import java.util.List;
import java.util.Map;

@Tag(name = "Cliente — Notificaciones", description = "Consulta de notificaciones transaccionales")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('CLIENTE')")
@RestController
@RequestMapping("/api/cliente/notificaciones")
public class NotificacionController {

    @Autowired private NotificacionService notificacionService;
    @Autowired private UsuarioRepository usuarioRepository;

    @Operation(summary = "Listar notificaciones recientes del cliente")
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificacionDTO>>> listar(Authentication auth) {
        Integer usuarioId = getUserId(auth);
        List<NotificacionDTO> data = notificacionService.listarNotificacionesUsuario(usuarioId);
        return ResponseEntity.ok(ApiResponse.ok("OK", data));
    }

    @Operation(summary = "Marcar notificación como leída")
    @PatchMapping("/{id}/leida")
    public ResponseEntity<ApiResponse<Void>> marcarLeida(@PathVariable Integer id, Authentication auth) {
        Integer usuarioId = getUserId(auth);
        notificacionService.marcarLeida(id, usuarioId);
        return ResponseEntity.ok(ApiResponse.ok("Notificación marcada como leída", null));
    }

    @Operation(summary = "Contar notificaciones no leídas")
    @GetMapping("/no-leidas/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> contarNoLeidas(Authentication auth) {
        Integer usuarioId = getUserId(auth);
        long count = notificacionService.contarNoLeidas(usuarioId);
        return ResponseEntity.ok(ApiResponse.ok("OK", Map.of("noLeidas", count)));
    }

    private Integer getUserId(Authentication auth) {
        String email = auth.getName();
        Usuario u = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return u.getId();
    }
}
