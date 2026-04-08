package pe.edu.utp.proyecto_integrador_casachantilly.pedido.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.proyecto_integrador_casachantilly.carrito.dto.CarritoDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.carrito.dto.CarritoItemDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.carrito.servicio.CarritoService;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.BadRequestException;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.ResourceNotFoundException;
import pe.edu.utp.proyecto_integrador_casachantilly.notificacion.servicio.NotificacionService;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.entidad.*;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.repositorio.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PedidoService {

    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private EstadoPedidoRepository estadoPedidoRepository;
    @Autowired private EstadoPedidoHistorialRepository historialRepository;
    @Autowired private CarritoService carritoService;
    @Autowired private NotificacionService notificacionService;

    /**
     * Crea un pedido congelando precios del carrito.
     * Inserta estado_pedido_historial con orden=1 (Pendiente de pago).
     */
    @Transactional
    public Pedido crearPedido(Integer carritoId, Integer usuarioId,
                              Integer direccionId, Integer franjaHorariaId,
                              Boolean esRecojoTienda, BigDecimal costoEnvio) {

        CarritoDTO resumen = carritoService.calcularResumen(carritoId);

        if (resumen.items().isEmpty()) {
            throw new BadRequestException("El carrito está vacío");
        }


        Pedido pedido = new Pedido();
        pedido.setUsuarioId(usuarioId);
        pedido.setDireccionId(direccionId);
        pedido.setFranjaHorariaId(franjaHorariaId);
        pedido.setSubtotal(resumen.subtotal());
        pedido.setImpuestos(resumen.igv());
        BigDecimal envio = costoEnvio != null ? costoEnvio : BigDecimal.ZERO;
        pedido.setCostoEnvio(envio);
        pedido.setTotal(resumen.total().add(envio));
        pedido.setEsRecojoTienda(esRecojoTienda != null && esRecojoTienda);
        pedido.setFechaActualizacion(LocalDateTime.now());

        pedidoRepository.save(pedido);
        pedido.setCodigoPedido(String.format("PED-%06d", pedido.getId()));


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


        EstadoPedido estadoPendiente = estadoPedidoRepository.findByOrden(1)
                .orElseThrow(() -> new ResourceNotFoundException("Estado 'Pendiente de pago' no encontrado"));

        EstadoPedidoHistorial hist = new EstadoPedidoHistorial();
        hist.setPedidoId(pedido.getId());
        hist.setEstadoId(estadoPendiente.getId());
        hist.setObservacion("Pedido creado — pendiente de pago");
        historialRepository.save(hist);

        pedido.setEstadoActualId(estadoPendiente.getId());
        pedido.setFechaActualizacion(LocalDateTime.now());
        pedidoRepository.save(pedido);

        notificacionService.registrarEventoPedido(
                usuarioId,
                pedido.getId(),
                "Pedido creado",
                "Tu pedido " + pedido.getCodigoPedido() + " fue creado y está pendiente de pago."
        );

        return pedido;
    }
}
