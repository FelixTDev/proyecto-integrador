package pe.edu.utp.proyecto_integrador_casachantilly.reclamo.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.repositorio.UsuarioRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.dto.ApiResponse;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.ResourceNotFoundException;
import pe.edu.utp.proyecto_integrador_casachantilly.reclamo.dto.ReclamoDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.reclamo.dto.ReclamoRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.reclamo.servicio.ReclamoService;

import java.util.List;

@Tag(name = "Cliente — Reclamos", description = "Gestión de reclamos del cliente")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('CLIENTE')")
@RestController
@RequestMapping("/api/cliente/reclamos")
public class ReclamoController {

    @Autowired private ReclamoService reclamoService;
    @Autowired private UsuarioRepository usuarioRepository;

    @Operation(summary = "Crear un reclamo")
    @PostMapping
    public ResponseEntity<ApiResponse<ReclamoDTO>> crear(
            @Valid @RequestBody ReclamoRequest req, Authentication auth) {
        Integer userId = getUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok("Reclamo registrado", reclamoService.crearReclamo(userId, req)));
    }

    @Operation(summary = "Listar reclamos del cliente")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReclamoDTO>>> listar(Authentication auth) {
        Integer userId = getUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok("OK", reclamoService.listarPorCliente(userId)));
    }

    private Integer getUserId(Authentication auth) {
        return usuarioRepository.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado")).getId();
    }
}
