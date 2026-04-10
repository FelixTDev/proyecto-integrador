package pe.edu.utp.proyecto_integrador_casachantilly.direccion.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.entidad.Usuario;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.repositorio.UsuarioRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.dto.ApiResponse;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.ResourceNotFoundException;
import pe.edu.utp.proyecto_integrador_casachantilly.direccion.dto.DireccionDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.direccion.dto.DireccionRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.direccion.dto.ZonaEnvioDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.direccion.servicio.DireccionService;

import java.util.List;

@Tag(name = "Cliente — Direcciones", description = "Gestión de direcciones de entrega del cliente")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('CLIENTE')")
@RestController
@RequestMapping("/api/cliente")
public class DireccionController {

    @Autowired private DireccionService direccionService;
    @Autowired private UsuarioRepository usuarioRepository;

    @Operation(summary = "Listar direcciones del cliente")
    @GetMapping("/direcciones")
    public ResponseEntity<ApiResponse<List<DireccionDTO>>> listarDirecciones(
            Authentication auth,
            @RequestParam(defaultValue = "false") boolean incluirInactivas) {
        Integer userId = getUserId(auth);
        List<DireccionDTO> data = direccionService.listarDirecciones(userId, incluirInactivas);
        return ResponseEntity.ok(ApiResponse.ok("OK", data));
    }

    @Operation(summary = "Crear dirección del cliente")
    @PostMapping("/direcciones")
    public ResponseEntity<ApiResponse<DireccionDTO>> crearDireccion(
            Authentication auth,
            @Valid @RequestBody DireccionRequest request) {
        Integer userId = getUserId(auth);
        DireccionDTO data = direccionService.crearDireccion(userId, request);
        return ResponseEntity.ok(ApiResponse.ok("Dirección creada", data));
    }

    @Operation(summary = "Editar dirección del cliente")
    @PutMapping("/direcciones/{direccionId}")
    public ResponseEntity<ApiResponse<DireccionDTO>> editarDireccion(
            Authentication auth,
            @PathVariable Integer direccionId,
            @Valid @RequestBody DireccionRequest request) {
        Integer userId = getUserId(auth);
        DireccionDTO data = direccionService.actualizarDireccion(userId, direccionId, request);
        return ResponseEntity.ok(ApiResponse.ok("Dirección actualizada", data));
    }

    @Operation(summary = "Desactivar dirección (eliminación lógica)")
    @PatchMapping("/direcciones/{direccionId}/desactivar")
    public ResponseEntity<ApiResponse<Void>> desactivarDireccion(
            Authentication auth,
            @PathVariable Integer direccionId) {
        Integer userId = getUserId(auth);
        direccionService.desactivarDireccion(userId, direccionId);
        return ResponseEntity.ok(ApiResponse.ok("Dirección desactivada", null));
    }

    @Operation(summary = "Marcar dirección principal")
    @PatchMapping("/direcciones/{direccionId}/principal")
    public ResponseEntity<ApiResponse<DireccionDTO>> marcarPrincipal(
            Authentication auth,
            @PathVariable Integer direccionId) {
        Integer userId = getUserId(auth);
        DireccionDTO data = direccionService.marcarPrincipal(userId, direccionId);
        return ResponseEntity.ok(ApiResponse.ok("Dirección principal actualizada", data));
    }

    @Operation(summary = "Listar zonas de envío activas")
    @GetMapping("/zonas-envio")
    public ResponseEntity<ApiResponse<List<ZonaEnvioDTO>>> listarZonasEnvio() {
        List<ZonaEnvioDTO> data = direccionService.listarZonasActivas();
        return ResponseEntity.ok(ApiResponse.ok("OK", data));
    }

    @Operation(summary = "Alias de compatibilidad: listar zonas de envío activas")
    @GetMapping("/direcciones/zonas")
    public ResponseEntity<ApiResponse<List<ZonaEnvioDTO>>> listarZonasEnvioCompat() {
        return listarZonasEnvio();
    }

    @Operation(summary = "Eliminar dirección (desactivación lógica)")
    @DeleteMapping("/direcciones/{direccionId}")
    public ResponseEntity<ApiResponse<Void>> eliminarDireccion(
            Authentication auth,
            @PathVariable Integer direccionId) {
        Integer userId = getUserId(auth);
        direccionService.desactivarDireccion(userId, direccionId);
        return ResponseEntity.ok(ApiResponse.ok("Dirección desactivada", null));
    }

    private Integer getUserId(Authentication auth) {
        String email = auth.getName();
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return usuario.getId();
    }
}
