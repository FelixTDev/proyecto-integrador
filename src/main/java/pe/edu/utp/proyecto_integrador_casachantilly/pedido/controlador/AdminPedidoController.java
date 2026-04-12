package pe.edu.utp.proyecto_integrador_casachantilly.pedido.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.entidad.Usuario;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.repositorio.UsuarioRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.dto.ApiResponse;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.ResourceNotFoundException;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto.AdminPedidoDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto.AdminPedidoEstadoRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto.AdminPedidoValidacionRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto.PedidoValidacionAuditoriaDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.servicio.PedidoGestionService;

import java.util.List;

@Tag(name = "Admin - Pedidos", description = "Validacion y gestion de estados de pedidos")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin/pedidos")
@PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
public class AdminPedidoController {

    @Autowired private PedidoGestionService pedidoGestionService;
    @Autowired private UsuarioRepository usuarioRepository;

    @Operation(summary = "Listar pedidos para panel administrativo")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminPedidoDTO>>> listarPedidos() {
        return ResponseEntity.ok(ApiResponse.ok("Listado de pedidos", pedidoGestionService.listarPedidosAdmin()));
    }

    @Operation(summary = "Validar pedido (aprobar/rechazar)")
    @PatchMapping("/{pedidoId}/validacion")
    public ResponseEntity<ApiResponse<AdminPedidoDTO>> validarPedido(
            @PathVariable Integer pedidoId,
            @Valid @RequestBody AdminPedidoValidacionRequest request,
            Authentication auth) {
        AdminPedidoDTO result = pedidoGestionService.validarPedido(pedidoId, request, getUserId(auth));
        return ResponseEntity.ok(ApiResponse.ok("Pedido validado", result));
    }

    @Operation(summary = "Cambiar estado de pedido")
    @PatchMapping("/{pedidoId}/estado")
    public ResponseEntity<ApiResponse<AdminPedidoDTO>> cambiarEstado(
            @PathVariable Integer pedidoId,
            @Valid @RequestBody AdminPedidoEstadoRequest request,
            Authentication auth) {
        AdminPedidoDTO result = pedidoGestionService.cambiarEstado(pedidoId, request, getUserId(auth));
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado", result));
    }

    @Operation(summary = "Listar historial de validacion del pedido")
    @GetMapping("/{pedidoId}/validacion/historial")
    public ResponseEntity<ApiResponse<List<PedidoValidacionAuditoriaDTO>>> historialValidacion(
            @PathVariable Integer pedidoId) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Historial de validacion",
                pedidoGestionService.listarAuditoriaValidacion(pedidoId)
        ));
    }

    private Integer getUserId(Authentication auth) {
        String email = auth.getName();
        Usuario u = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return u.getId();
    }
}