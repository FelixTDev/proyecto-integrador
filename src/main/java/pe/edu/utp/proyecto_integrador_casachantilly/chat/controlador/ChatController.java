package pe.edu.utp.proyecto_integrador_casachantilly.chat.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.repositorio.UsuarioRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.chat.dto.ChatMensajeDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.chat.dto.ChatMensajeRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.chat.servicio.ChatService;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.dto.ApiResponse;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.ResourceNotFoundException;

import java.util.List;
import java.util.Map;

@Tag(name = "Chat", description = "Mensajería por pedido")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired private ChatService chatService;
    @Autowired private UsuarioRepository usuarioRepository;

    @Operation(summary = "Enviar mensaje en un pedido")
    @PostMapping("/{pedidoId}")
    public ResponseEntity<ApiResponse<ChatMensajeDTO>> enviar(
            @PathVariable Integer pedidoId,
            @Valid @RequestBody ChatMensajeRequest req,
            Authentication auth) {
        UserContext ctx = getUserContext(auth);
        ChatMensajeDTO dto = chatService.enviarMensaje(pedidoId, ctx.userId(), ctx.esStaff(), req.mensaje());
        return ResponseEntity.ok(ApiResponse.ok("Mensaje enviado", dto));
    }

    @Operation(summary = "Listar mensajes de un pedido")
    @GetMapping("/{pedidoId}")
    public ResponseEntity<ApiResponse<List<ChatMensajeDTO>>> listar(
            @PathVariable Integer pedidoId, Authentication auth) {
        UserContext ctx = getUserContext(auth);
        return ResponseEntity.ok(ApiResponse.ok("Mensajes", chatService.listarMensajes(pedidoId, ctx.userId(), ctx.esStaff())));
    }

    @Operation(summary = "Contar mensajes no leídos")
    @GetMapping("/no-leidos")
    public ResponseEntity<ApiResponse<Map<String, Long>>> noLeidos(Authentication auth) {
        UserContext ctx = getUserContext(auth);
        long count = ctx.esStaff() ? chatService.contarNoLeidosAdmin() : chatService.contarNoLeidosCliente(ctx.userId());
        return ResponseEntity.ok(ApiResponse.ok("OK", Map.of("noLeidos", count)));
    }

    private UserContext getUserContext(Authentication auth) {
        var usuario = usuarioRepository.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        boolean esStaff = usuario.getRoles().stream()
                .anyMatch(r -> "ADMIN".equals(r.getNombre()));
        return new UserContext(usuario.getId(), esStaff);
    }

    private record UserContext(Integer userId, boolean esStaff) {}
}
