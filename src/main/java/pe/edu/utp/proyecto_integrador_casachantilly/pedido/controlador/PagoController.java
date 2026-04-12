package pe.edu.utp.proyecto_integrador_casachantilly.pedido.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.entidad.Usuario;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.repositorio.UsuarioRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.dto.ApiResponse;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.ResourceNotFoundException;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto.PagoCotizacionDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto.PagoCotizacionRequestDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto.PagoRequestDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto.PagoResultDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.servicio.PagoService;

@Tag(name = "Pagos", description = "Procesamiento de pagos con Culqi")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    @Autowired private PagoService pagoService;
    @Autowired private UsuarioRepository usuarioRepository;

    @Operation(summary = "Cotizar total del checkout")
    @PostMapping("/cotizacion")
    public ResponseEntity<ApiResponse<PagoCotizacionDTO>> cotizar(
            Authentication auth,
            @Valid @RequestBody PagoCotizacionRequestDTO request) {
        Integer userId = getUserId(auth);
        PagoCotizacionDTO dto = pagoService.cotizar(
                request.carritoId(),
                userId,
                request.direccionId(),
                request.esRecojoTienda(),
                request.franjaHorariaId(),
                request.zonaEntrega(),
                request.esUrgente(),
                request.codigoCupon()
        );
        return ResponseEntity.ok(ApiResponse.ok("Cotizacion calculada", dto));
    }

    @Operation(summary = "Procesar pago del carrito")
    @PostMapping("/procesar")
    public ResponseEntity<ApiResponse<PagoResultDTO>> procesarPago(
            Authentication auth,
            @Valid @RequestBody PagoRequestDTO request) {
        Integer userId = getUserId(auth);
        PagoResultDTO result = pagoService.procesarPago(request, userId);
        String msg = result.aprobado() ? "Pago aprobado" : "Pago rechazado";
        return ResponseEntity.ok(new ApiResponse<>(result.aprobado(), msg, result));
    }

    private Integer getUserId(Authentication auth) {
        String email = auth.getName();
        Usuario u = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return u.getId();
    }
}