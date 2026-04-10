package pe.edu.utp.proyecto_integrador_casachantilly.fidelidad.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.repositorio.UsuarioRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.dto.ApiResponse;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.ResourceNotFoundException;
import pe.edu.utp.proyecto_integrador_casachantilly.fidelidad.dto.PuntosSaldoDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.fidelidad.servicio.PuntosFidelidadService;

import java.util.Map;

@Tag(name = "Cliente — Puntos", description = "Programa de fidelidad")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('CLIENTE')")
@RestController
@RequestMapping("/api/cliente/puntos")
public class PuntosFidelidadController {

    @Autowired private PuntosFidelidadService puntosService;
    @Autowired private UsuarioRepository usuarioRepository;

    @Operation(summary = "Ver saldo y movimientos de puntos")
    @GetMapping
    public ResponseEntity<ApiResponse<PuntosSaldoDTO>> saldo(Authentication auth) {
        Integer userId = getUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok("Puntos de fidelidad", puntosService.obtenerSaldoYMovimientos(userId)));
    }

    @Operation(summary = "Canjear puntos")
    @PostMapping("/canjear")
    public ResponseEntity<ApiResponse<PuntosSaldoDTO>> canjear(
            @RequestBody Map<String, Integer> body, Authentication auth) {
        Integer userId = getUserId(auth);
        Integer puntos = body.get("puntos");
        Integer pedidoId = body.get("pedidoId");
        PuntosSaldoDTO result = puntosService.canjearPuntos(userId, puntos, pedidoId);
        return ResponseEntity.ok(ApiResponse.ok("Puntos canjeados", result));
    }

    private Integer getUserId(Authentication auth) {
        return usuarioRepository.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado")).getId();
    }
}
