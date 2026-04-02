package pe.edu.utp.proyecto_integrador_casachantilly.carrito.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.entidad.Usuario;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.repositorio.UsuarioRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.carrito.dto.CarritoDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.carrito.dto.CarritoItemRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.carrito.entidad.Carrito;
import pe.edu.utp.proyecto_integrador_casachantilly.carrito.servicio.CarritoService;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.dto.ApiResponse;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.ResourceNotFoundException;

@Tag(name = "Carrito", description = "Gestión del carrito de compras")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    @Autowired private CarritoService carritoService;
    @Autowired private UsuarioRepository usuarioRepository;

    @Operation(summary = "Ver carrito del usuario autenticado")
    @GetMapping
    public ResponseEntity<ApiResponse<CarritoDTO>> getCarrito(Authentication auth) {
        Integer userId = getUserId(auth);
        Carrito carrito = carritoService.getCarritoActivo(userId);
        CarritoDTO dto = carritoService.calcularResumen(carrito.getId());
        return ResponseEntity.ok(ApiResponse.ok("OK", dto));
    }

    @Operation(summary = "Agregar ítem al carrito")
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CarritoDTO>> addItem(
            Authentication auth,
            @Valid @RequestBody CarritoItemRequest req) {
        Integer userId = getUserId(auth);
        Carrito carrito = carritoService.getCarritoActivo(userId);
        CarritoDTO dto = carritoService.addItem(carrito.getId(), req.varianteId(), req.cantidad());
        return ResponseEntity.ok(ApiResponse.ok("Ítem agregado", dto));
    }

    @Operation(summary = "Actualizar cantidad de un ítem")
    @PutMapping("/items/{detalleId}")
    public ResponseEntity<ApiResponse<CarritoDTO>> updateItem(
            @PathVariable Integer detalleId,
            @RequestParam int cantidad) {
        CarritoDTO dto = carritoService.updateItem(detalleId, cantidad);
        return ResponseEntity.ok(ApiResponse.ok("Cantidad actualizada", dto));
    }

    @Operation(summary = "Eliminar ítem del carrito")
    @DeleteMapping("/items/{detalleId}")
    public ResponseEntity<ApiResponse<CarritoDTO>> removeItem(@PathVariable Integer detalleId) {
        CarritoDTO dto = carritoService.removeItem(detalleId);
        return ResponseEntity.ok(ApiResponse.ok("Ítem eliminado", dto));
    }

    @Operation(summary = "Vaciar el carrito completo")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> vaciar(Authentication auth) {
        Integer userId = getUserId(auth);
        Carrito carrito = carritoService.getCarritoActivo(userId);
        carritoService.vaciarCarrito(carrito.getId());
        return ResponseEntity.ok(ApiResponse.ok("Carrito vaciado", null));
    }

    private Integer getUserId(Authentication auth) {
        String email = auth.getName();
        Usuario u = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return u.getId();
    }
}
