package pe.edu.utp.proyecto_integrador_casachantilly.catalogo.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.dto.CrearProductoRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.dto.InventarioMovimientoDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.dto.InventarioMovimientoRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.dto.ProductoDetalleDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.entidad.Alergeno;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.entidad.Categoria;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.entidad.InventarioMovimiento;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.entidad.Producto;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.entidad.ProductoVariante;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.repositorio.AlergenoRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.repositorio.CategoriaRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.repositorio.InventarioMovimientoRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.repositorio.ProductoRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.repositorio.ProductoVarianteRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.BadRequestException;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class AdminProductoService {

    @Autowired private ProductoRepository productoRepository;
    @Autowired private ProductoVarianteRepository varianteRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private AlergenoRepository alergenoRepository;
    @Autowired private InventarioMovimientoRepository inventarioRepository;
    @Autowired private CatalogoService catalogoService;

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<pe.edu.utp.proyecto_integrador_casachantilly.catalogo.dto.ProductoCardDTO> getTodosProductosPaginados(org.springframework.data.domain.Pageable pageable) {
        return productoRepository.findAll(pageable).map(catalogoService::toCardDTO);
    }

    @Transactional
    public ProductoDetalleDTO crearProducto(CrearProductoRequest req) {
        Categoria categoria = categoriaRepository.findById(req.categoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada: " + req.categoriaId()));

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

        Producto reloaded = productoRepository.findById(saved.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Error al recargar producto"));
        return catalogoService.toDetalleDTO(reloaded);
    }

    @Transactional
    public ProductoDetalleDTO editarProducto(Integer id, CrearProductoRequest req) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + id));

        Categoria categoria = categoriaRepository.findById(req.categoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada: " + req.categoriaId()));

        producto.setNombre(req.nombre());
        producto.setDescripcion(req.descripcion());
        producto.setCategoria(categoria);
        if (req.activo() != null) {
            producto.setActivo(req.activo());
        }
        producto.setSlug(slugify(req.nombre()));
        producto.setFechaActualizacion(LocalDateTime.now());

        if (req.alergenoIds() != null) {
            Set<Alergeno> alergenos = new HashSet<>(alergenoRepository.findAllById(req.alergenoIds()));
            producto.setAlergenos(alergenos);
        }

        productoRepository.save(producto);

        if (req.variantes() != null && !req.variantes().isEmpty()) {
            varianteRepository.desactivarPorProductoId(id);
            for (CrearProductoRequest.VarianteRequest vReq : req.variantes()) {
                varianteRepository.save(buildVariante(vReq, producto));
            }
        }

        Producto reloaded = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Error al recargar producto"));
        return catalogoService.toDetalleDTO(reloaded);
    }

    @Transactional
    public boolean toggleProducto(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + id));
        producto.setActivo(!producto.getActivo());
        producto.setFechaActualizacion(LocalDateTime.now());
        productoRepository.save(producto);
        return producto.getActivo();
    }

    @Transactional
    public void eliminarProductoLogico(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + id));

        if (!Boolean.TRUE.equals(producto.getActivo())) {
            return;
        }

        producto.setActivo(false);
        producto.setFechaActualizacion(LocalDateTime.now());
        productoRepository.save(producto);

        varianteRepository.desactivarPorProductoId(id);
    }

    @Transactional
    public void registrarMovimientoInventario(Integer varianteId, InventarioMovimientoRequest req) {
        ProductoVariante variante = varianteRepository.findById(varianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Variante no encontrada: " + varianteId));

        int nuevoStock = switch (req.tipo()) {
            case ENTRADA -> variante.getStockDisponible() + req.cantidad();
            case SALIDA -> {
                if (variante.getStockDisponible() < req.cantidad()) {
                    throw new BadRequestException("Stock insuficiente. Disponible: " + variante.getStockDisponible());
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

    @Transactional(readOnly = true)
    public List<InventarioMovimientoDTO> listarMovimientosInventario(Integer varianteId, LocalDateTime desde, LocalDateTime hasta) {
        varianteRepository.findById(varianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Variante no encontrada: " + varianteId));

        LocalDateTime fechaDesde = desde == null ? LocalDateTime.now().minusDays(30) : desde;
        LocalDateTime fechaHasta = hasta == null ? LocalDateTime.now() : hasta;
        if (fechaHasta.isBefore(fechaDesde)) {
            throw new BadRequestException("El rango de fechas es invalido");
        }

        return inventarioRepository.findByVarianteIdAndFechaBetweenOrderByFechaDesc(varianteId, fechaDesde, fechaHasta)
                .stream()
                .map(this::toInventarioDto)
                .toList();
    }

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
        if (base.length() > 43) {
            base = base.substring(0, 43);
        }
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase(Locale.ROOT);
        v.setCodigoSku("SKU-" + base.toUpperCase(Locale.ROOT) + "-" + suffix);
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

    private InventarioMovimientoDTO toInventarioDto(InventarioMovimiento mov) {
        return new InventarioMovimientoDTO(
                mov.getId(),
                mov.getVariante().getId(),
                mov.getTipo(),
                mov.getCantidad(),
                mov.getStockResultante(),
                mov.getMotivo(),
                mov.getPedidoId(),
                mov.getUsuarioId(),
                mov.getFecha()
        );
    }
}