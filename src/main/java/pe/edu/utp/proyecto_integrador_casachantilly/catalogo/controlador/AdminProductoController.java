package pe.edu.utp.proyecto_integrador_casachantilly.catalogo.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.dto.CrearProductoRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.dto.InventarioMovimientoDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.dto.InventarioMovimientoRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.dto.ProductoCardDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.dto.ProductoDetalleDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.servicio.AdminProductoService;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.dto.ApiResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "Admin - Productos", description = "Gestion de productos (requiere rol ADMIN)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductoController {

    @Autowired
    private AdminProductoService adminProductoService;

    @Operation(summary = "Obtener todos los productos (activos e inactivos)")
    @GetMapping("/productos")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<ProductoCardDTO>>> getTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                page, size, org.springframework.data.domain.Sort.by("id").descending());
        org.springframework.data.domain.Page<ProductoCardDTO> result = adminProductoService.getTodosProductosPaginados(pageable);
        return ResponseEntity.ok(ApiResponse.ok("Listado completo", result));
    }

    @Operation(summary = "Crear nuevo producto con variantes")
    @PostMapping("/productos")
    public ResponseEntity<ApiResponse<ProductoDetalleDTO>> crear(
            @Valid @RequestBody CrearProductoRequest request) {
        ProductoDetalleDTO created = adminProductoService.crearProducto(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Producto creado exitosamente", created));
    }

    @Operation(summary = "Editar producto existente")
    @PutMapping("/productos/{id}")
    public ResponseEntity<ApiResponse<ProductoDetalleDTO>> editar(
            @PathVariable Integer id,
            @Valid @RequestBody CrearProductoRequest request) {
        ProductoDetalleDTO updated = adminProductoService.editarProducto(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Producto actualizado", updated));
    }

    @Operation(summary = "Activar o desactivar producto")
    @PatchMapping("/productos/{id}/toggle")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggle(@PathVariable Integer id) {
        boolean nuevoEstado = adminProductoService.toggleProducto(id);
        return ResponseEntity.ok(ApiResponse.ok(
                "Estado actualizado",
                Map.of("id", id, "activo", nuevoEstado)
        ));
    }

    @Operation(summary = "Eliminar producto (desactivacion logica)")
    @DeleteMapping("/productos/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> eliminar(@PathVariable Integer id) {
        adminProductoService.eliminarProductoLogico(id);
        return ResponseEntity.ok(ApiResponse.ok(
                "Producto eliminado logicamente",
                Map.of("id", id, "activo", false)
        ));
    }

    @Operation(summary = "Registrar movimiento de inventario para una variante")
    @PostMapping("/variantes/{id}/inventario")
    public ResponseEntity<ApiResponse<Void>> registrarMovimiento(
            @PathVariable Integer id,
            @Valid @RequestBody InventarioMovimientoRequest request) {
        adminProductoService.registrarMovimientoInventario(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Movimiento registrado exitosamente", null));
    }

    @Operation(summary = "Listar movimientos de inventario por variante y rango de fechas")
    @GetMapping("/variantes/{id}/inventario/movimientos")
    public ResponseEntity<ApiResponse<List<InventarioMovimientoDTO>>> listarMovimientos(
            @PathVariable Integer id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Movimientos de inventario",
                adminProductoService.listarMovimientosInventario(id, desde, hasta)
        ));
    }
}