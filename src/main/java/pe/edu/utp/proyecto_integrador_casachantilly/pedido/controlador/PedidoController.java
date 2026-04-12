package pe.edu.utp.proyecto_integrador_casachantilly.pedido.controlador;

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
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto.ComprobantePedidoDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto.PedidoHistorialDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto.ReordenarRequestDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto.ReordenarResultadoDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.servicio.PedidoGestionService;

import java.util.List;

@Tag(name = "Pedidos", description = "Consulta y comprobantes de pedidos")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired private PedidoGestionService pedidoGestionService;
    @Autowired private UsuarioRepository usuarioRepository;

    @Operation(summary = "Obtener comprobante de pedido")
    @GetMapping("/{pedidoId}/comprobante")
    public ResponseEntity<ApiResponse<ComprobantePedidoDTO>> obtenerComprobante(
            @PathVariable Integer pedidoId,
            Authentication auth) {
        UserContext ctx = getUser(auth);
        ComprobantePedidoDTO data = pedidoGestionService.obtenerComprobante(pedidoId, ctx.userId(), ctx.isAdmin());
        return ResponseEntity.ok(ApiResponse.ok("Comprobante generado", data));
    }

    @Operation(summary = "Listar historial de pedidos del cliente autenticado")
    @PreAuthorize("hasAnyRole('CLIENTE','ADMIN')")
    @GetMapping("/mis-pedidos")
    public ResponseEntity<ApiResponse<List<PedidoHistorialDTO>>> listarHistorial(Authentication auth) {
        UserContext ctx = getUser(auth);
        List<PedidoHistorialDTO> data = pedidoGestionService.listarHistorialCliente(ctx.userId());
        return ResponseEntity.ok(ApiResponse.ok("Historial obtenido", data));
    }

    @Operation(summary = "Reordenar un pedido previo hacia el carrito actual")
    @PreAuthorize("hasAnyRole('CLIENTE','ADMIN')")
    @PostMapping("/{pedidoId}/reordenar")
    public ResponseEntity<ApiResponse<ReordenarResultadoDTO>> reordenar(
            @PathVariable Integer pedidoId,
            @RequestBody(required = false) ReordenarRequestDTO request,
            Authentication auth) {
        UserContext ctx = getUser(auth);
        ReordenarResultadoDTO data = pedidoGestionService.reordenarPedido(pedidoId, ctx.userId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Reordenación procesada", data));
    }

    private UserContext getUser(Authentication auth) {
        String email = auth.getName();
        Usuario u = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        boolean isAdmin = u.getRoles().stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getNombre()));
        return new UserContext(u.getId(), isAdmin);
    }

    private record UserContext(Integer userId, boolean isAdmin) {}
}
