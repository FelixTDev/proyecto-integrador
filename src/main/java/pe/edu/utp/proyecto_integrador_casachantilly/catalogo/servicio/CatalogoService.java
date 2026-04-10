package pe.edu.utp.proyecto_integrador_casachantilly.catalogo.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.dto.ProductoCardDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.dto.ProductoDetalleDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.dto.ProductoVarianteDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.entidad.Alergeno;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.entidad.Producto;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.entidad.ProductoVariante;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.repositorio.ProductoRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.ResourceNotFoundException;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.servicio.CapacidadProduccionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class CatalogoService {

    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private CapacidadProduccionService capacidadProduccionService;



    /**
     * RF01 — Listado paginado de productos activos.
     * Si categoriaId == null devuelve todos; si no, filtra por categoría.
     */
    @Transactional(readOnly = true)
    public Page<ProductoCardDTO> getProductosPaginados(Integer categoriaId, Pageable pageable) {
        Page<Producto> page = (categoriaId != null && categoriaId > 0)
                ? productoRepository.findByCategoriaIdAndActivoTrue(categoriaId, pageable)
                : productoRepository.findByActivoTrue(pageable);

        boolean hayCapacidadProduccion = capacidadProduccionService.hayCapacidadParaFecha(LocalDate.now());
        return page.map(p -> toCardDTO(p, hayCapacidadProduccion));
    }

    /**
     * RF03 — Detalle de un producto con variantes y alérgenos.
     */
    @Transactional(readOnly = true)
    public ProductoDetalleDTO getDetalleProducto(Integer productoId) {
        Producto p = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + productoId));
        boolean hayCapacidadProduccion = capacidadProduccionService.hayCapacidadParaFecha(LocalDate.now());
        return toDetalleDTO(p, hayCapacidadProduccion);
    }



    public ProductoCardDTO toCardDTO(Producto p) {
        return toCardDTO(p, true);
    }

    public ProductoCardDTO toCardDTO(Producto p, boolean hayCapacidadProduccion) {
        List<ProductoVariante> variantes = p.getVariantes();

        BigDecimal precioMinimo = variantes.stream()
                .filter(v -> Boolean.TRUE.equals(v.getActivo()) && v.getStockDisponible() > 0)
                .map(ProductoVariante::getPrecio)
                .min(BigDecimal::compareTo)
                .orElse(variantes.stream()
                        .map(ProductoVariante::getPrecio)
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO));

        boolean hayStockVariante = variantes.stream()
                .anyMatch(v -> Boolean.TRUE.equals(v.getActivo()) && v.getStockDisponible() > 0);
        boolean hayStock = hayStockVariante && hayCapacidadProduccion;

        String desc = p.getDescripcion();
        if (desc != null && desc.length() > 150) {
            desc = desc.substring(0, 147) + "...";
        }

        List<String> alergenos = p.getAlergenos().stream()
                .map(Alergeno::getNombre)
                .toList();

        return new ProductoCardDTO(
                p.getId(), p.getNombre(), desc, precioMinimo,
                p.getImagenUrl(),  // Map imagenUrl a urlFotoPortada
                p.getCategoria().getNombre(), alergenos, hayStock,
                p.getActivo()
        );
    }

    public ProductoDetalleDTO toDetalleDTO(Producto p) {
        return toDetalleDTO(p, true);
    }

    public ProductoDetalleDTO toDetalleDTO(Producto p, boolean hayCapacidadProduccion) {
        List<String> alergenos = p.getAlergenos().stream()
                .map(Alergeno::getNombre)
                .toList();

        List<ProductoVarianteDTO> variantes = p.getVariantes().stream()
                .map(v -> new ProductoVarianteDTO(
                        v.getId(), v.getNombreVariante(), v.getPrecio(), v.getCosto(),
                        v.getPesoGramos(), v.getTiempoPrepMin(), v.getStockDisponible(),
                        Boolean.TRUE.equals(v.getActivo()) && hayCapacidadProduccion
                ))
                .toList();

        return new ProductoDetalleDTO(
                p.getId(), p.getNombre(), p.getDescripcion(), p.getActivo(),
                p.getCategoria().getId(), p.getCategoria().getNombre(),
                p.getImagenUrl(), alergenos, variantes
        );
    }
}
