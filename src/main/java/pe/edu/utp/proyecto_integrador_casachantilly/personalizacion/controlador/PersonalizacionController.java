package pe.edu.utp.proyecto_integrador_casachantilly.personalizacion.controlador;

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
import pe.edu.utp.proyecto_integrador_casachantilly.personalizacion.dto.PersonalizacionDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.personalizacion.dto.PersonalizacionRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.personalizacion.servicio.PersonalizacionService;

import java.util.List;

@Tag(name = "Cliente — Personalización", description = "Personalización de pedidos especiales")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('CLIENTE','ADMIN')")
@RestController
@RequestMapping("/api/pedidos/{pedidoId}/personalizacion")
public class PersonalizacionController {

    @Autowired private PersonalizacionService personalizacionService;
    @Autowired private UsuarioRepository usuarioRepository;

    @Operation(summary = "Guardar/actualizar personalización de un ítem del pedido")
    @PostMapping("/items/{detalleId}")
    public ResponseEntity<ApiResponse<PersonalizacionDTO>> guardar(
            @PathVariable Integer pedidoId,
            @PathVariable Integer detalleId,
            @RequestBody PersonalizacionRequest req,
            Authentication auth) {
        Integer userId = getUserId(auth);
        PersonalizacionDTO dto = personalizacionService.guardar(pedidoId, detalleId, userId, req);
        return ResponseEntity.ok(ApiResponse.ok("Personalización guardada", dto));
    }

    @Operation(summary = "Listar personalizaciones del pedido")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PersonalizacionDTO>>> listar(
            @PathVariable Integer pedidoId, Authentication auth) {
        Integer userId = getUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok("OK", personalizacionService.listarPorPedido(pedidoId, userId)));
    }

    private Integer getUserId(Authentication auth) {
        return usuarioRepository.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado")).getId();
    }
}
