package pe.edu.utp.proyecto_integrador_casachantilly.reclamo.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.dto.ApiResponse;
import pe.edu.utp.proyecto_integrador_casachantilly.reclamo.dto.ReclamoDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.reclamo.dto.ReclamoEstadoRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.reclamo.servicio.ReclamoService;

import java.util.List;

@Tag(name = "Admin — Reclamos", description = "Gestión administrativa de reclamos")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin/reclamos")
public class AdminReclamoController {

    @Autowired private ReclamoService reclamoService;

    @Operation(summary = "Listar todos los reclamos")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReclamoDTO>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok("Reclamos", reclamoService.listarTodos()));
    }

    @Operation(summary = "Actualizar estado de reclamo")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ReclamoDTO>> actualizarEstado(
            @PathVariable Integer id, @RequestBody ReclamoEstadoRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Reclamo actualizado", reclamoService.actualizarEstado(id, req)));
    }
}
