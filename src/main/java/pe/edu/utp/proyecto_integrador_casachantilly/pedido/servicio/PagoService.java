package pe.edu.utp.proyecto_integrador_casachantilly.pedido.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.proyecto_integrador_casachantilly.carrito.dto.CarritoDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.carrito.dto.CarritoItemDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.carrito.servicio.CarritoService;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.entidad.ProductoVariante;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.repositorio.ProductoVarianteRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.BadRequestException;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.ResourceNotFoundException;
import pe.edu.utp.proyecto_integrador_casachantilly.direccion.entidad.Direccion;
import pe.edu.utp.proyecto_integrador_casachantilly.direccion.repositorio.ZonaEnvioRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.direccion.servicio.DireccionService;
import pe.edu.utp.proyecto_integrador_casachantilly.notificacion.servicio.NotificacionService;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto.PagoCotizacionDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto.PagoRequestDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto.PagoResultDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.entidad.EstadoPedido;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.entidad.EstadoPedidoHistorial;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.entidad.Pago;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.entidad.Pedido;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.repositorio.EstadoPedidoHistorialRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.repositorio.EstadoPedidoRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.repositorio.MetodoPagoRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.repositorio.PagoRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.repositorio.PedidoRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;

@Service
public class PagoService {

    private static final int CUPO_MAXIMO_FRANJA = 20;

    @Autowired private CulqiService culqiService;
    @Autowired private PedidoService pedidoService;
    @Autowired private CarritoService carritoService;
    @Autowired private ProductoVarianteRepository varianteRepository;
    @Autowired private DireccionService direccionService;
    @Autowired private ZonaEnvioRepository zonaEnvioRepository;
    @Autowired private PagoRepository pagoRepository;
    @Autowired private MetodoPagoRepository metodoPagoRepository;
    @Autowired private EstadoPedidoRepository estadoPedidoRepository;
    @Autowired private EstadoPedidoHistorialRepository historialRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private NotificacionService notificacionService;

    @Transactional(readOnly = true)
    public PagoCotizacionDTO cotizar(Integer carritoId, Integer usuarioId, Integer direccionId,
                                     Boolean esRecojoTienda, Integer franjaHorariaId, String zonaEntrega) {
        CarritoDTO resumen = carritoService.calcularResumen(carritoId);
        if (resumen.items().isEmpty()) {
            throw new BadRequestException("El carrito está vacío");
        }

        if (franjaHorariaId == null) {
            throw new BadRequestException("Debes seleccionar una franja horaria");
        }

        boolean franjaDisponible = isFranjaDisponible(franjaHorariaId);
        BigDecimal costoEnvio = resolverCostoEnvio(usuarioId, direccionId, esRecojoTienda, zonaEntrega);

        return new PagoCotizacionDTO(
                carritoId,
                resumen.subtotal(),
                resumen.igv(),
                costoEnvio,
                resumen.total().add(costoEnvio),
                franjaDisponible,
                franjaDisponible ? "Franja disponible" : "La franja seleccionada no tiene cupos disponibles"
        );
    }

    /**
     * Flujo completo de pago:
     * 1) Revalida stock y franja
     * 2) Crea el pedido congelando precios
     * 3) Llama a Culqi con token de tarjeta
     * 4) Si APROBADO: descuenta stock, guarda pago, confirma estado y vacía carrito
     */
    @Transactional
    public PagoResultDTO procesarPago(PagoRequestDTO req, Integer usuarioId) {
        metodoPagoRepository.findById(req.metodoPagoId())
                .orElseThrow(() -> new ResourceNotFoundException("Método de pago no encontrado"));

        CarritoDTO resumen = carritoService.calcularResumen(req.carritoId());
        if (resumen.items().isEmpty()) {
            throw new BadRequestException("El carrito está vacío");
        }

        if (req.franjaHorariaId() == null) {
            throw new BadRequestException("Debes seleccionar una franja horaria");
        }
        if (!isFranjaDisponible(req.franjaHorariaId())) {
            throw new BadRequestException("La franja horaria seleccionada ya no tiene cupos disponibles");
        }

        validarStockDisponible(resumen.items());
        BigDecimal costoEnvio = resolverCostoEnvio(usuarioId, req.direccionId(), req.esRecojoTienda(), req.zonaEntrega());
        Integer direccionPedidoId = resolveDireccionPedidoId(usuarioId, req.direccionId(), req.esRecojoTienda());

        Pedido pedido = pedidoService.crearPedido(
                req.carritoId(), usuarioId,
                direccionPedidoId, req.franjaHorariaId(),
                req.esRecojoTienda(), costoEnvio
        );

        BigDecimal totalConEnvio = resumen.total().add(costoEnvio);
        int montoCentimos = totalConEnvio.multiply(BigDecimal.valueOf(100)).intValue();

        String email = req.email() != null ? req.email() : "cliente@casachantilly.pe";
        Map<String, Object> culqiResult = culqiService.crearCargo(req.tokenTarjeta(), montoCentimos, email);

        boolean aprobado = (boolean) culqiResult.get("aprobado");
        String referencia = (String) culqiResult.get("referencia");
        String mensaje = (String) culqiResult.get("mensaje");

        Pago pago = new Pago();
        pago.setPedidoId(pedido.getId());
        pago.setMetodoPagoId(req.metodoPagoId());
        pago.setMonto(totalConEnvio);
        pago.setMoneda("PEN");
        pago.setReferenciaExterna(referencia);
        pago.setIdTransaccionExterna(referencia);

        if (aprobado) {
            descontarStock(resumen.items());

            pago.setEstado(Pago.EstadoPago.APROBADO);
            pago.setIntentos(1);
            pago.setFechaAprobacion(LocalDateTime.now());
            pagoRepository.save(pago);

            EstadoPedido estadoConfirmado = estadoPedidoRepository.findByOrden(2)
                    .orElseThrow(() -> new ResourceNotFoundException("Estado 'Pago confirmado' no encontrado"));

            EstadoPedidoHistorial hist = new EstadoPedidoHistorial();
            hist.setPedidoId(pedido.getId());
            hist.setEstadoId(estadoConfirmado.getId());
            hist.setObservacion("Pago aprobado — ref: " + referencia);
            historialRepository.save(hist);

            pedido.setEstadoActualId(estadoConfirmado.getId());
            pedido.setFechaActualizacion(LocalDateTime.now());

            carritoService.vaciarCarrito(req.carritoId());

            notificacionService.registrarEventoPedido(
                    usuarioId,
                    pedido.getId(),
                    "Pedido confirmado",
                    "Tu pedido " + (pedido.getCodigoPedido() != null ? pedido.getCodigoPedido() : "#" + pedido.getId())
                            + " fue confirmado exitosamente."
            );

            return new PagoResultDTO(true, mensaje, pedido.getId(),
                    pago.getId(), referencia, totalConEnvio, 1);
        }

        pago.setEstado(Pago.EstadoPago.RECHAZADO);
        pago.setIntentos(1);
        pago.setProximoIntento(LocalDateTime.now().plusMinutes(5));
        pagoRepository.save(pago);

        return new PagoResultDTO(false, mensaje, pedido.getId(),
                pago.getId(), referencia, totalConEnvio, 1);
    }

    private BigDecimal resolverCostoEnvio(Integer usuarioId, Integer direccionId, Boolean esRecojoTienda, String zonaEntrega) {
        if (Boolean.TRUE.equals(esRecojoTienda)) {
            return BigDecimal.ZERO;
        }

        if (direccionId == null) {
            throw new BadRequestException("Debes seleccionar una dirección de entrega");
        }

        Direccion direccion = direccionService.obtenerDireccionActivaUsuario(usuarioId, direccionId);
        if (direccion.getZonaId() != null) {
            return zonaEnvioRepository.findByIdAndActivoTrue(direccion.getZonaId())
                    .map(z -> z.getCostoDelivery())
                    .orElseGet(() -> fallbackCostoEnvio(zonaEntrega != null ? zonaEntrega : direccion.getDireccionCompleta()));
        }
        return fallbackCostoEnvio(zonaEntrega != null ? zonaEntrega : direccion.getDireccionCompleta());
    }

    private Integer resolveDireccionPedidoId(Integer usuarioId, Integer direccionId, Boolean esRecojoTienda) {
        if (Boolean.TRUE.equals(esRecojoTienda)) {
            return null;
        }
        if (direccionId == null) {
            throw new BadRequestException("Debes seleccionar una dirección de entrega");
        }
        Direccion direccion = direccionService.obtenerDireccionActivaUsuario(usuarioId, direccionId);
        return direccion.getId();
    }

    private BigDecimal fallbackCostoEnvio(String zonaEntrega) {
        String zona = zonaEntrega == null ? "" : zonaEntrega.trim().toLowerCase(Locale.ROOT);
        if (zona.isBlank()) {
            return new BigDecimal("15.00");
        }
        if (zona.contains("lince") || zona.contains("san isidro")) {
            return new BigDecimal("8.00");
        }
        if (zona.contains("miraflores") || zona.contains("jesus maria") || zona.contains("surquillo")) {
            return new BigDecimal("10.00");
        }
        return new BigDecimal("15.00");
    }

    private boolean isFranjaDisponible(Integer franjaHorariaId) {
        if (franjaHorariaId == null || franjaHorariaId <= 0) {
            return false;
        }
        LocalDateTime inicio = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime fin = inicio.plusDays(1);
        long pedidosEnFranja = pedidoRepository.countByFranjaHorariaIdAndFechaCreacionBetween(franjaHorariaId, inicio, fin);
        return pedidosEnFranja < CUPO_MAXIMO_FRANJA;
    }

    private void validarStockDisponible(Iterable<CarritoItemDTO> items) {
        for (CarritoItemDTO item : items) {
            ProductoVariante variante = varianteRepository.findByIdForUpdate(item.varianteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Variante no encontrada: " + item.varianteId()));
            if (!Boolean.TRUE.equals(variante.getActivo())) {
                throw new BadRequestException("La variante '" + item.varianteNombre() + "' no está activa");
            }
            if (variante.getStockDisponible() < item.cantidad()) {
                throw new BadRequestException("Stock insuficiente para '" + item.productoNombre()
                        + " - " + item.varianteNombre() + "'. Disponible: " + variante.getStockDisponible());
            }
        }
    }

    private void descontarStock(Iterable<CarritoItemDTO> items) {
        for (CarritoItemDTO item : items) {
            ProductoVariante variante = varianteRepository.findByIdForUpdate(item.varianteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Variante no encontrada: " + item.varianteId()));
            int nuevoStock = variante.getStockDisponible() - item.cantidad();
            if (nuevoStock < 0) {
                throw new BadRequestException("No se pudo descontar stock para '" + item.productoNombre() + "'");
            }
            variante.setStockDisponible(nuevoStock);
            varianteRepository.save(variante);
        }
    }
}
