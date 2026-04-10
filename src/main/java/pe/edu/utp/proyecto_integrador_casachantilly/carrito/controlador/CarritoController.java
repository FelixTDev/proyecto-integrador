package pe.edu.utp.proyecto_integrador_casachantilly.carrito.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.entidad.Usuario;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.repositorio.UsuarioRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.carrito.dto.CarritoDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.carrito.dto.CarritoItemRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.carrito.entidad.Carrito;
import pe.edu.utp.proyecto_integrador_casachantilly.carrito.servicio.CarritoService;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.dto.ApiResponse;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.ResourceNotFoundException;

@Tag(name = "Carrito", description = "Gestion del carrito de compras")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Operation(summary = "Ver carrito del usuario autenticado")
    @GetMapping
    public ResponseEntity<ApiResponse<CarritoDTO>> getCarrito(Authentication auth) {
        Integer userId = getUserId(auth);
        Carrito carrito = carritoService.getCarritoActivo(userId);
        CarritoDTO dto = carritoService.calcularResumen(carrito.getId());
        return ResponseEntity.ok(ApiResponse.ok("OK", dto));
    }

    @Operation(summary = "Agregar item al carrito")
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CarritoDTO>> addItem(
            Authentication auth,
            @Valid @RequestBody CarritoItemRequest req) {
        Integer userId = getUserId(auth);
        Carrito carrito = carritoService.getCarritoActivo(userId);
        CarritoDTO dto = carritoService.addItem(carrito.getId(), req.varianteId(), req.cantidad());
        return ResponseEntity.ok(ApiResponse.ok("Item agregado", dto));
    }

    @Operation(summary = "Actualizar cantidad de un item")
    @PutMapping("/items/{detalleId}")
    public ResponseEntity<ApiResponse<CarritoDTO>> updateItem(
            Authentication auth,
            @PathVariable Integer detalleId,
            @RequestParam int cantidad) {
        Integer userId = getUserId(auth);
        CarritoDTO dto = carritoService.updateItem(detalleId, userId, cantidad);
        return ResponseEntity.ok(ApiResponse.ok("Cantidad actualizada", dto));
    }

    @Operation(summary = "Eliminar item del carrito")
    @DeleteMapping("/items/{detalleId}")
    public ResponseEntity<ApiResponse<CarritoDTO>> removeItem(
            Authentication auth,
            @PathVariable Integer detalleId) {
        Integer userId = getUserId(auth);
        CarritoDTO dto = carritoService.removeItem(detalleId, userId);
        return ResponseEntity.ok(ApiResponse.ok("Item eliminado", dto));
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
        Usuario u = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return u.getId();
    }
}
