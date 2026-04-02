package pe.edu.utp.proyecto_integrador_casachantilly.catalogo.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.dto.ProductoCardDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.dto.ProductoDetalleDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.entidad.Categoria;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.repositorio.CategoriaRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.servicio.CatalogoService;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.dto.ApiResponse;

import java.util.List;

@Tag(name = "Catálogo", description = "Endpoints públicos del catálogo de productos")
@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {

    @Autowired
    private CatalogoService catalogoService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    /**
     * RF01 — Listado paginado con filtro opcional de categoría.
     * GET /api/catalogo?categoria=1&page=0&size=12
     */
    @Operation(summary = "Obtener productos paginados por categoría")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductoCardDTO>>> getProductos(
            @RequestParam(required = false) Integer categoria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        Page<ProductoCardDTO> result = catalogoService.getProductosPaginados(categoria, pageable);
        return ResponseEntity.ok(ApiResponse.ok("OK", result));
    }

    /**
     * RF03 — Detalle de un producto por ID.
     * GET /api/catalogo/{id}
     */
    @Operation(summary = "Obtener detalle de un producto")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoDetalleDTO>> getDetalle(@PathVariable Integer id) {
        ProductoDetalleDTO detalle = catalogoService.getDetalleProducto(id);
        return ResponseEntity.ok(ApiResponse.ok("OK", detalle));
    }

    /**
     * Endpoint auxiliar para el frontend — lista de categorías activas.
     * GET /api/catalogo/categorias
     */
    @Operation(summary = "Listar categorías activas")
    @GetMapping("/categorias")
    public ResponseEntity<ApiResponse<List<Categoria>>> getCategorias() {
        List<Categoria> cats = categoriaRepository.findByActivoTrueOrderByNombreAsc();
        return ResponseEntity.ok(ApiResponse.ok("OK", cats));
    }
}
