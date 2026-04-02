package pe.edu.utp.proyecto_integrador_casachantilly.pedido.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.proyecto_integrador_casachantilly.carrito.dto.CarritoDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.carrito.dto.CarritoItemDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.carrito.servicio.CarritoService;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.BadRequestException;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.ResourceNotFoundException;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.entidad.*;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.repositorio.*;

import java.math.BigDecimal;

@Service
public class PedidoService {

    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private EstadoPedidoRepository estadoPedidoRepository;
    @Autowired private EstadoPedidoHistorialRepository historialRepository;
    @Autowired private CarritoService carritoService;

    /**
     * Crea un pedido congelando precios del carrito.
     * Inserta estado_pedido_historial con orden=1 (Pendiente de pago).
     */
    @Transactional
    public Pedido crearPedido(Integer carritoId, Integer usuarioId,
                              Integer direccionId, Integer franjaHorariaId,
                              Boolean esRecojoTienda) {

        CarritoDTO resumen = carritoService.calcularResumen(carritoId);

        if (resumen.items().isEmpty()) {
            throw new BadRequestException("El carrito está vacío");
        }

        // Crear pedido
        Pedido pedido = new Pedido();
        pedido.setUsuarioId(usuarioId);
        pedido.setDireccionId(direccionId);
        pedido.setFranjaHorariaId(franjaHorariaId);
        pedido.setSubtotal(resumen.subtotal());
        pedido.setImpuestos(resumen.igv());
        pedido.setTotal(resumen.total());
        pedido.setEsRecojoTienda(esRecojoTienda != null && esRecojoTienda);

        pedidoRepository.save(pedido);

        // Crear detalles con precio snapshot
        for (CarritoItemDTO item : resumen.items()) {
            DetallePedido det = new DetallePedido();
            det.setPedido(pedido);
            det.setVarianteId(item.varianteId());
            det.setNombreSnapshot(item.productoNombre() + " - " + item.varianteNombre());
            det.setPrecioUnitarioSnapshot(item.precioUnitario());
            det.setCantidad(item.cantidad());
            det.setSubtotalLinea(item.subtotal());
            pedido.getDetalles().add(det);
        }

        pedidoRepository.save(pedido);

        // Registrar estado "Pendiente de pago" (orden=1)
        EstadoPedido estadoPendiente = estadoPedidoRepository.findByOrden(1)
                .orElseThrow(() -> new ResourceNotFoundException("Estado 'Pendiente de pago' no encontrado"));

        EstadoPedidoHistorial hist = new EstadoPedidoHistorial();
        hist.setPedidoId(pedido.getId());
        hist.setEstadoId(estadoPendiente.getId());
        hist.setObservacion("Pedido creado — pendiente de pago");
        historialRepository.save(hist);

        return pedido;
    }
}
