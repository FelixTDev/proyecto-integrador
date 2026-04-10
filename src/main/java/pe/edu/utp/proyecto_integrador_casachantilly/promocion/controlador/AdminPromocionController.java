package pe.edu.utp.proyecto_integrador_casachantilly.promocion.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.dto.ApiResponse;
import pe.edu.utp.proyecto_integrador_casachantilly.promocion.dto.CuponResultDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.promocion.dto.PromocionDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.promocion.dto.PromocionRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.promocion.servicio.PromocionService;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Admin — Promociones", description = "CRUD y motor de promociones")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin/promociones")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPromocionController {

    @Autowired private PromocionService promocionService;

    @Operation(summary = "Listar todas las promociones")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PromocionDTO>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok("Promociones", promocionService.listarTodas()));
    }

    @Operation(summary = "Crear nueva promoción")
    @PostMapping
    public ResponseEntity<ApiResponse<PromocionDTO>> crear(@Valid @RequestBody PromocionRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Promoción creada", promocionService.crear(req)));
    }

    @Operation(summary = "Editar promoción")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PromocionDTO>> editar(@PathVariable Integer id,
            @Valid @RequestBody PromocionRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Promoción editada", promocionService.editar(id, req)));
    }

    @Operation(summary = "Activar/desactivar promoción")
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<Boolean>> toggle(@PathVariable Integer id) {
        boolean activo = promocionService.toggleActivo(id);
        return ResponseEntity.ok(ApiResponse.ok(activo ? "Activada" : "Desactivada", activo));
    }
}
