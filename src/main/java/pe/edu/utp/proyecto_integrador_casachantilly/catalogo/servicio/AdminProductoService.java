package pe.edu.utp.proyecto_integrador_casachantilly.catalogo.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.dto.CrearProductoRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.dto.ProductoDetalleDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.dto.InventarioMovimientoRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.entidad.*;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.repositorio.*;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.BadRequestException;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.ResourceNotFoundException;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.time.LocalDateTime;

@Service
public class AdminProductoService {

    @Autowired private ProductoRepository productoRepository;
    @Autowired private ProductoVarianteRepository varianteRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private AlergenoRepository alergenoRepository;
    @Autowired private InventarioMovimientoRepository inventarioRepository;
    @Autowired private CatalogoService catalogoService;

    // ─── Crear producto (RF02) ────────────────────────────────────────────────

    @Transactional
    public ProductoDetalleDTO crearProducto(CrearProductoRequest req) {
        Categoria categoria = categoriaRepository.findById(req.categoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada: " + req.categoriaId()));

        Producto producto = new Producto();
        producto.setNombre(req.nombre());
        producto.setDescripcion(req.descripcion());
        producto.setCategoria(categoria);
        producto.setActivo(req.activo() != null ? req.activo() : true);
        producto.setSlug(slugify(req.nombre()));
        producto.setFechaActualizacion(LocalDateTime.now());

        if (req.alergenoIds() != null && !req.alergenoIds().isEmpty()) {
            Set<Alergeno> alergenos = new HashSet<>(alergenoRepository.findAllById(req.alergenoIds()));
            producto.setAlergenos(alergenos);
        }

        Producto saved = productoRepository.save(producto);

        for (CrearProductoRequest.VarianteRequest vReq : req.variantes()) {
            ProductoVariante variante = buildVariante(vReq, saved);
            varianteRepository.save(variante);
        }

        // Reload to get full relationships
        Producto reloaded = productoRepository.findById(saved.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Error al recargar producto"));
        return catalogoService.toDetalleDTO(reloaded);
    }

    // ─── Editar producto (RF02) ────────────────────────────────────────────────

    @Transactional
    public ProductoDetalleDTO editarProducto(Integer id, CrearProductoRequest req) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + id));

        Categoria categoria = categoriaRepository.findById(req.categoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada: " + req.categoriaId()));

        producto.setNombre(req.nombre());
        producto.setDescripcion(req.descripcion());
        producto.setCategoria(categoria);
        if (req.activo() != null) producto.setActivo(req.activo());
        producto.setSlug(slugify(req.nombre()));
        producto.setFechaActualizacion(LocalDateTime.now());

        if (req.alergenoIds() != null) {
            Set<Alergeno> alergenos = new HashSet<>(alergenoRepository.findAllById(req.alergenoIds()));
            producto.setAlergenos(alergenos);
        }

        productoRepository.save(producto);

        // Reemplazar variantes si se envían
        if (req.variantes() != null && !req.variantes().isEmpty()) {
            // No se elimina físicamente para conservar trazabilidad/histórico
            varianteRepository.desactivarPorProductoId(id);
            for (CrearProductoRequest.VarianteRequest vReq : req.variantes()) {
                varianteRepository.save(buildVariante(vReq, producto));
            }
        }

        Producto reloaded = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Error al recargar producto"));
        return catalogoService.toDetalleDTO(reloaded);
    }

    // ─── Toggle activo/inactivo (RF05) ─────────────────────────────────────────

    @Transactional
    public boolean toggleProducto(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + id));
        producto.setActivo(!producto.getActivo());
        producto.setFechaActualizacion(LocalDateTime.now());
        productoRepository.save(producto);
        return producto.getActivo();
    }

    // ─── Movimiento de inventario (RF05) ───────────────────────────────────────

    @Transactional
    public void registrarMovimientoInventario(Integer varianteId, InventarioMovimientoRequest req) {
        ProductoVariante variante = varianteRepository.findById(varianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Variante no encontrada: " + varianteId));

        int nuevoStock = switch (req.tipo()) {
            case ENTRADA -> variante.getStockDisponible() + req.cantidad();
            case SALIDA -> {
                if (variante.getStockDisponible() < req.cantidad()) {
                    throw new BadRequestException(
                            "Stock insuficiente. Disponible: " + variante.getStockDisponible());
                }
                yield variante.getStockDisponible() - req.cantidad();
            }
            case AJUSTE -> req.cantidad();
        };

        variante.setStockDisponible(nuevoStock);
        varianteRepository.save(variante);

        InventarioMovimiento mov = new InventarioMovimiento();
        mov.setVariante(variante);
        mov.setTipo(req.tipo());
        mov.setCantidad(req.cantidad());
        mov.setStockResultante(nuevoStock);
        mov.setMotivo(req.motivo());
        inventarioRepository.save(mov);
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private ProductoVariante buildVariante(CrearProductoRequest.VarianteRequest vReq, Producto producto) {
        ProductoVariante v = new ProductoVariante();
        v.setProducto(producto);
        v.setNombreVariante(vReq.nombreVariante());
        v.setPrecio(vReq.precio());
        v.setCosto(vReq.costo());
        v.setPesoGramos(vReq.pesoGramos());
        v.setTiempoPrepMin(vReq.tiempoPrepMin() != null ? vReq.tiempoPrepMin() : 60);
        v.setStockDisponible(vReq.stockDisponible() != null ? vReq.stockDisponible() : 0);
        v.setActivo(vReq.activo() != null ? vReq.activo() : true);
        String base = slugify(producto.getNombre()) + "-" + slugify(vReq.nombreVariante());
        if (base.length() > 50) {
            base = base.substring(0, 50);
        }
        v.setCodigoSku("SKU-" + base.toUpperCase(Locale.ROOT));
        return v;
    }

    private String slugify(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        return input
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-{2,}", "-");
    }
}
