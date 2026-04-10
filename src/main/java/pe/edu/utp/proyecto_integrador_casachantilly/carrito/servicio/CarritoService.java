package pe.edu.utp.proyecto_integrador_casachantilly.carrito.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.entidad.Usuario;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.repositorio.UsuarioRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.carrito.dto.CarritoDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.carrito.dto.CarritoItemDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.carrito.entidad.Carrito;
import pe.edu.utp.proyecto_integrador_casachantilly.carrito.entidad.CarritoDetalle;
import pe.edu.utp.proyecto_integrador_casachantilly.carrito.repositorio.CarritoDetalleRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.carrito.repositorio.CarritoRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.entidad.ProductoVariante;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.repositorio.ProductoVarianteRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.BadRequestException;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.ResourceNotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CarritoService {

    private static final BigDecimal IGV_RATE = new BigDecimal("0.18");

    @Autowired private CarritoRepository carritoRepository;
    @Autowired private CarritoDetalleRepository detalleRepository;
    @Autowired private ProductoVarianteRepository varianteRepository;
    @Autowired private UsuarioRepository usuarioRepository;



    @Transactional
    public Carrito getCarritoActivo(Integer usuarioId) {
        return carritoRepository.findFirstByUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .orElseGet(() -> {
                    Usuario usuario = usuarioRepository.findById(usuarioId)
                            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
                    Carrito nuevo = new Carrito();
                    nuevo.setUsuario(usuario);
                    return carritoRepository.save(nuevo);
                });
    }



    @Transactional
    public CarritoDTO addItem(Integer carritoId, Integer varianteId, int cantidad) {
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado"));

        ProductoVariante variante = varianteRepository.findById(varianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Variante no encontrada"));

        if (!variante.getActivo()) {
            throw new BadRequestException("Esta variante no está activa");
        }


        if (variante.getStockDisponible() < cantidad) {
            throw new BadRequestException(
                    "Stock insuficiente. Disponible: " + variante.getStockDisponible()
                            + ", solicitado: " + cantidad);
        }


        var existente = detalleRepository.findByCarritoIdAndVarianteId(carritoId, varianteId);
        if (existente.isPresent()) {
            CarritoDetalle det = existente.get();
            int nuevaCantidad = det.getCantidad() + cantidad;
            if (variante.getStockDisponible() < nuevaCantidad) {
                throw new BadRequestException(
                        "Stock insuficiente para " + nuevaCantidad + " unidades. Disponible: "
                                + variante.getStockDisponible());
            }
            det.setCantidad(nuevaCantidad);
            detalleRepository.save(det);
        } else {
            CarritoDetalle det = new CarritoDetalle();
            det.setCarrito(carrito);
            det.setVariante(variante);
            det.setCantidad(cantidad);
            detalleRepository.save(det);
        }

        carrito.setFechaActualizacion(LocalDateTime.now());
        carritoRepository.save(carrito);

        return calcularResumen(carritoId);
    }



    @Transactional
    public CarritoDTO updateItem(Integer detalleId, Integer usuarioId, int cantidad) {
        CarritoDetalle det = detalleRepository.findByIdAndCarritoUsuarioId(detalleId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Ítem no encontrado para el usuario"));

        if (cantidad <= 0) {
            detalleRepository.delete(det);
        } else {
            ProductoVariante v = det.getVariante();
            if (v.getStockDisponible() < cantidad) {
                throw new BadRequestException("Stock insuficiente. Disponible: " + v.getStockDisponible());
            }
            det.setCantidad(cantidad);
            detalleRepository.save(det);
        }

        return calcularResumen(det.getCarrito().getId());
    }



    @Transactional
    public CarritoDTO removeItem(Integer detalleId, Integer usuarioId) {
        CarritoDetalle det = detalleRepository.findByIdAndCarritoUsuarioId(detalleId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Ítem no encontrado para el usuario"));
        Integer carritoId = det.getCarrito().getId();
        detalleRepository.delete(det);
        return calcularResumen(carritoId);
    }



    @Transactional(readOnly = true)
    public CarritoDTO calcularResumen(Integer carritoId) {
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado"));

        List<CarritoDetalle> detalles = detalleRepository.findByCarritoId(carritoId);

        List<CarritoItemDTO> items = detalles.stream().map(d -> {
            ProductoVariante v = d.getVariante();
            BigDecimal sub = v.getPrecio().multiply(BigDecimal.valueOf(d.getCantidad()));
            return new CarritoItemDTO(
                    d.getId(), v.getId(),
                    v.getProducto().getNombre(), v.getNombreVariante(),
                    v.getPrecio(), d.getCantidad(), sub,
                    v.getStockDisponible(),
                    v.getProducto().getImagenUrl()
            );
        }).toList();

        BigDecimal subtotal = items.stream()
                .map(CarritoItemDTO::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal igv = subtotal.multiply(IGV_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(igv);

        return new CarritoDTO(
                carrito.getId(),
                carrito.getUsuario().getId(),
                items, subtotal, igv, total
        );
    }



    @Transactional
    public void vaciarCarrito(Integer carritoId) {
        detalleRepository.deleteByCarritoId(carritoId);
    }
}
