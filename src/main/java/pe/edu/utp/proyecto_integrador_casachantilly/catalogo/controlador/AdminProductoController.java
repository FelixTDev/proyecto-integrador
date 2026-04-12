package pe.edu.utp.proyecto_integrador_casachantilly.catalogo.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.dto.CrearProductoRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.dto.InventarioMovimientoRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.dto.ProductoDetalleDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.servicio.AdminProductoService;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.dto.ApiResponse;

import java.util.Map;

@Tag(name = "Admin — Productos", description = "Gestión de productos (requiere rol ADMIN)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductoController {

    @Autowired
    private AdminProductoService adminProductoService;

    /** RF01/RF02 — Listado completo para administrador. GET /api/admin/productos */
    @Operation(summary = "Obtener todos los productos (activos e inactivos)")
    @GetMapping("/productos")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<pe.edu.utp.proyecto_integrador_casachantilly.catalogo.dto.ProductoCardDTO>>> getTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("id").descending());
        org.springframework.data.domain.Page<pe.edu.utp.proyecto_integrador_casachantilly.catalogo.dto.ProductoCardDTO> result = adminProductoService.getTodosProductosPaginados(pageable);
        return ResponseEntity.ok(ApiResponse.ok("Listado completo", result));
    }

    /** RF02 — Crear producto con variantes. POST /api/admin/productos */
    @Operation(summary = "Crear nuevo producto con variantes")
    @PostMapping("/productos")
    public ResponseEntity<ApiResponse<ProductoDetalleDTO>> crear(
            @Valid @RequestBody CrearProductoRequest request) {
        ProductoDetalleDTO created = adminProductoService.crearProducto(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Producto creado exitosamente", created));
    }

    /** RF02 — Editar producto. PUT /api/admin/productos/{id} */
    @Operation(summary = "Editar producto existente")
    @PutMapping("/productos/{id}")
    public ResponseEntity<ApiResponse<ProductoDetalleDTO>> editar(
            @PathVariable Integer id,
            @Valid @RequestBody CrearProductoRequest request) {
        ProductoDetalleDTO updated = adminProductoService.editarProducto(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Producto actualizado", updated));
    }

    /** RF05 — Activar/desactivar producto. PATCH /api/admin/productos/{id}/toggle */
    @Operation(summary = "Activar o desactivar producto")
    @PatchMapping("/productos/{id}/toggle")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggle(@PathVariable Integer id) {
        boolean nuevoEstado = adminProductoService.toggleProducto(id);
        return ResponseEntity.ok(ApiResponse.ok(
                "Estado actualizado",
                Map.of("id", id, "activo", nuevoEstado)
        ));
    }

    /** RF05 — Eliminar producto (baja lógica). DELETE /api/admin/productos/{id} */
    @Operation(summary = "Eliminar producto (desactivación lógica)")
    @DeleteMapping("/productos/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> eliminar(@PathVariable Integer id) {
        adminProductoService.eliminarProductoLogico(id);
        return ResponseEntity.ok(ApiResponse.ok(
                "Producto eliminado logicamente",
                Map.of("id", id, "activo", false)
        ));
    }

    /** RF05 — Registrar movimiento de inventario. POST /api/admin/variantes/{id}/inventario */
    @Operation(summary = "Registrar movimiento de inventario para una variante")
    @PostMapping("/variantes/{id}/inventario")
    public ResponseEntity<ApiResponse<Void>> registrarMovimiento(
            @PathVariable Integer id,
            @Valid @RequestBody InventarioMovimientoRequest request) {
        adminProductoService.registrarMovimientoInventario(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Movimiento registrado exitosamente", null));
    }
}
