package pe.edu.utp.proyecto_integrador_casachantilly.promocion.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.dto.ApiResponse;
import pe.edu.utp.proyecto_integrador_casachantilly.promocion.dto.CuponResultDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.promocion.dto.PromocionDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.promocion.dto.ValidarCuponRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.promocion.servicio.PromocionService;

import java.util.List;

@Tag(name = "Cliente - Promociones", description = "Consulta y aplicacion de cupones")
@RestController
@RequestMapping("/api/promociones")
public class PromocionClienteController {

    @Autowired private PromocionService promocionService;

    @Operation(summary = "Listar promociones vigentes")
    @GetMapping("/vigentes")
    public ResponseEntity<ApiResponse<List<PromocionDTO>>> vigentes() {
        return ResponseEntity.ok(ApiResponse.ok("Promociones vigentes", promocionService.listarVigentes()));
    }

    @Operation(summary = "Validar cupon")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('CLIENTE','ADMIN')")
    @PostMapping("/validar-cupon")
    public ResponseEntity<ApiResponse<CuponResultDTO>> validarCupon(@Valid @RequestBody ValidarCuponRequest body) {
        CuponResultDTO result = promocionService.validarCupon(body.codigoCupon(), body.subtotal(), body.costoEnvio());
        return ResponseEntity.ok(ApiResponse.ok(result.mensaje(), result));
    }
}