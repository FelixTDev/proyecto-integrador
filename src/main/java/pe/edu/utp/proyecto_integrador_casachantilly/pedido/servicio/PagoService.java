package pe.edu.utp.proyecto_integrador_casachantilly.pedido.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.proyecto_integrador_casachantilly.carrito.dto.CarritoDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.carrito.servicio.CarritoService;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.BadRequestException;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.ResourceNotFoundException;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto.PagoRequestDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto.PagoResultDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.entidad.*;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.repositorio.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class PagoService {

    @Autowired private CulqiService culqiService;
    @Autowired private PedidoService pedidoService;
    @Autowired private CarritoService carritoService;
    @Autowired private PagoRepository pagoRepository;
    @Autowired private MetodoPagoRepository metodoPagoRepository;
    @Autowired private EstadoPedidoRepository estadoPedidoRepository;
    @Autowired private EstadoPedidoHistorialRepository historialRepository;

    /**
     * Flujo completo de pago:
     * 1) Crea el pedido congelando precios
     * 2) Llama a Culqi con token de tarjeta
     * 3) Si APROBADO: pago.estado=APROBADO, crea historial estado=2, vacía carrito
     * 4) Si RECHAZADO: pago.estado=RECHAZADO, incrementa intentos, calcula próximo_intento
     */
    @Transactional
    public PagoResultDTO procesarPago(PagoRequestDTO req, Integer usuarioId) {
        // Validar método de pago
        metodoPagoRepository.findById(req.metodoPagoId())
                .orElseThrow(() -> new ResourceNotFoundException("Método de pago no encontrado"));

        // Calcular resumen del carrito
        CarritoDTO resumen = carritoService.calcularResumen(req.carritoId());
        if (resumen.items().isEmpty()) {
            throw new BadRequestException("El carrito está vacío");
        }

        // 1) Crear pedido
        Pedido pedido = pedidoService.crearPedido(
                req.carritoId(), usuarioId,
                req.direccionId(), req.franjaHorariaId(),
                req.esRecojoTienda()
        );

        // 2) Llamar a pasarela Culqi
        int montoCentimos = resumen.total()
                .multiply(BigDecimal.valueOf(100))
                .intValue();

        String email = req.email() != null ? req.email() : "cliente@casachantilly.pe";
        Map<String, Object> culqiResult = culqiService.crearCargo(
                req.tokenTarjeta(), montoCentimos, email
        );

        boolean aprobado = (boolean) culqiResult.get("aprobado");
        String referencia = (String) culqiResult.get("referencia");
        String mensaje = (String) culqiResult.get("mensaje");

        // 3) Registrar pago
        Pago pago = new Pago();
        pago.setPedidoId(pedido.getId());
        pago.setMetodoPagoId(req.metodoPagoId());
        pago.setMonto(resumen.total());
        pago.setReferenciaExterna(referencia);

        if (aprobado) {
            pago.setEstado(Pago.EstadoPago.APROBADO);
            pago.setIntentos(1);
            pagoRepository.save(pago);

            // Registrar estado "Pago confirmado" (orden=2)
            EstadoPedido estadoConfirmado = estadoPedidoRepository.findByOrden(2)
                    .orElseThrow(() -> new ResourceNotFoundException("Estado 'Pago confirmado' no encontrado"));

            EstadoPedidoHistorial hist = new EstadoPedidoHistorial();
            hist.setPedidoId(pedido.getId());
            hist.setEstadoId(estadoConfirmado.getId());
            hist.setObservacion("Pago aprobado — ref: " + referencia);
            historialRepository.save(hist);

            // Vaciar carrito
            carritoService.vaciarCarrito(req.carritoId());

            return new PagoResultDTO(true, mensaje, pedido.getId(),
                    pago.getId(), referencia, resumen.total(), 1);
        } else {
            pago.setEstado(Pago.EstadoPago.RECHAZADO);
            pago.setIntentos(1);
            pago.setProximoIntento(LocalDateTime.now().plusMinutes(5));
            pagoRepository.save(pago);

            return new PagoResultDTO(false, mensaje, pedido.getId(),
                    pago.getId(), referencia, resumen.total(), 1);
        }
    }
}
