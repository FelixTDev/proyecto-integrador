package pe.edu.utp.proyecto_integrador_casachantilly.promocion.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.dto.ApiResponse;
import pe.edu.utp.proyecto_integrador_casachantilly.promocion.dto.CuponResultDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.promocion.dto.PromocionDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.promocion.servicio.PromocionService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Tag(name = "Cliente — Promociones", description = "Consulta y aplicación de cupones")
@RestController
@RequestMapping("/api/promociones")
public class PromocionClienteController {

    @Autowired private PromocionService promocionService;

    @Operation(summary = "Listar promociones vigentes")
    @GetMapping("/vigentes")
    public ResponseEntity<ApiResponse<List<PromocionDTO>>> vigentes() {
        return ResponseEntity.ok(ApiResponse.ok("Promociones vigentes", promocionService.listarVigentes()));
    }

    @Operation(summary = "Validar cupón")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('CLIENTE')")
    @PostMapping("/validar-cupon")
    public ResponseEntity<ApiResponse<CuponResultDTO>> validarCupon(@RequestBody Map<String, Object> body) {
        String codigo = (String) body.get("codigoCupon");
        BigDecimal subtotal = new BigDecimal(String.valueOf(body.get("subtotal")));
        CuponResultDTO result = promocionService.validarCupon(codigo, subtotal);
        return ResponseEntity.ok(ApiResponse.ok(result.mensaje(), result));
    }
}
