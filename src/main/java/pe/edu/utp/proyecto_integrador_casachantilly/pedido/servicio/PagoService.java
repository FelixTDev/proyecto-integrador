package pe.edu.utp.proyecto_integrador_casachantilly.pedido.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import pe.edu.utp.proyecto_integrador_casachantilly.entrega.entidad.FranjaHoraria;
import pe.edu.utp.proyecto_integrador_casachantilly.entrega.repositorio.FranjaHorariaRepository;
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
import pe.edu.utp.proyecto_integrador_casachantilly.promocion.dto.CuponResultDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.promocion.servicio.PromocionService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class PagoService {

    private static final int CUPO_MAXIMO_FRANJA = 20;

    @Autowired private PagoGateway pagoGateway;
    @Autowired private PedidoService pedidoService;
    @Autowired private CarritoService carritoService;
    @Autowired private ProductoVarianteRepository varianteRepository;
    @Autowired private DireccionService direccionService;
    @Autowired private ZonaEnvioRepository zonaEnvioRepository;
    @Autowired private FranjaHorariaRepository franjaHorariaRepository;
    @Autowired private PagoRepository pagoRepository;
    @Autowired private MetodoPagoRepository metodoPagoRepository;
    @Autowired private EstadoPedidoRepository estadoPedidoRepository;
    @Autowired private EstadoPedidoHistorialRepository historialRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private NotificacionService notificacionService;
    @Autowired private CapacidadProduccionService capacidadProduccionService;
    @Autowired private PromocionService promocionService;

    @Value("${app.envio.recargo-urgencia:5.00}")
    private BigDecimal recargoUrgencia;

    @Transactional(readOnly = true)
    public PagoCotizacionDTO cotizar(Integer carritoId,
                                     Integer usuarioId,
                                     Integer direccionId,
                                     Boolean esRecojoTienda,
                                     Integer franjaHorariaId,
                                     String zonaEntrega,
                                     Boolean esUrgente,
                                     String codigoCupon) {
        CarritoDTO resumen = carritoService.calcularResumen(carritoId);
        if (resumen.items().isEmpty()) {
            throw new BadRequestException("El carrito esta vacio");
        }
        if (franjaHorariaId == null) {
            throw new BadRequestException("Debes seleccionar una franja horaria");
        }

        boolean recojo = Boolean.TRUE.equals(esRecojoTienda);
        boolean urgente = Boolean.TRUE.equals(esUrgente);

        validarCompatibilidadFranja(franjaHorariaId, recojo);
        boolean franjaDisponible = isFranjaDisponible(franjaHorariaId);
        validarCapacidadOperativa(franjaHorariaId);

        BigDecimal costoEnvioBase = resolverCostoEnvio(usuarioId, direccionId, recojo, zonaEntrega);
        BigDecimal recargo = urgente ? obtenerRecargoUrgencia(recojo) : BigDecimal.ZERO;
        BigDecimal costoEnvioCalculado = costoEnvioBase.add(recargo);

        CuponResultDTO cupon = resolverCupon(codigoCupon, resumen.subtotal(), costoEnvioCalculado);
        BigDecimal descuento = cupon.descuento();
        BigDecimal totalBruto = resumen.subtotal().add(resumen.igv()).add(costoEnvioCalculado);
        BigDecimal totalFinal = totalBruto.subtract(descuento).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        return new PagoCotizacionDTO(
                carritoId,
                resumen.subtotal(),
                resumen.igv(),
                descuento,
                cupon.costoEnvioFinal(),
                totalFinal,
                franjaDisponible,
                franjaDisponible ? "Franja disponible" : "La franja seleccionada no tiene cupos disponibles",
                codigoCupon,
                cupon.tipoDescuento(),
                cupon.envioGratisAplicado(),
                cupon.costoEnvioOriginal(),
                urgente,
                recargo
        );
    }

    @Transactional
    public PagoResultDTO procesarPago(PagoRequestDTO req, Integer usuarioId) {
        metodoPagoRepository.findById(req.metodoPagoId())
                .orElseThrow(() -> new ResourceNotFoundException("Metodo de pago no encontrado"));

        String idemKey = normalizarIdempotencyKey(req.idempotencyKey());
        if (idemKey != null) {
            var previo = pagoRepository.findFirstByIdempotencyKeyOrderByFechaDesc(idemKey);
            if (previo.isPresent()) {
                Pago existente = previo.get();
                return new PagoResultDTO(
                        Pago.EstadoPago.APROBADO.equals(existente.getEstado()),
                        "Operacion idempotente: se devolvio el pago previamente registrado",
                        existente.getPedidoId(),
                        existente.getId(),
                        existente.getReferenciaExterna(),
                        existente.getMonto(),
                        existente.getIntentos(),
                        existente.getEstado().name(),
                        existente.getIdempotencyKey(),
                        existente.getCodigoErrorProveedor()
                );
            }
        }

        CarritoDTO resumen = carritoService.calcularResumen(req.carritoId());
        if (resumen.items().isEmpty()) {
            throw new BadRequestException("El carrito esta vacio");
        }

        if (req.franjaHorariaId() == null) {
            throw new BadRequestException("Debes seleccionar una franja horaria");
        }

        boolean recojo = Boolean.TRUE.equals(req.esRecojoTienda());
        boolean urgente = Boolean.TRUE.equals(req.esUrgente());

        validarCompatibilidadFranja(req.franjaHorariaId(), recojo);
        if (!isFranjaDisponible(req.franjaHorariaId())) {
            throw new BadRequestException("La franja horaria seleccionada ya no tiene cupos disponibles");
        }
        validarCapacidadOperativa(req.franjaHorariaId());
        validarStockDisponible(resumen.items());

        BigDecimal costoEnvioBase = resolverCostoEnvio(usuarioId, req.direccionId(), recojo, req.zonaEntrega());
        BigDecimal recargo = urgente ? obtenerRecargoUrgencia(recojo) : BigDecimal.ZERO;
        BigDecimal costoEnvioCalculado = costoEnvioBase.add(recargo);

        CuponResultDTO cupon = resolverCupon(req.codigoCupon(), resumen.subtotal(), costoEnvioCalculado);
        BigDecimal descuento = cupon.descuento();

        Integer direccionPedidoId = resolveDireccionPedidoId(usuarioId, req.direccionId(), recojo);
        Pedido pedido = pedidoService.crearPedido(
                req.carritoId(),
                usuarioId,
                direccionPedidoId,
                req.franjaHorariaId(),
                recojo,
                cupon.costoEnvioFinal()
        );

        BigDecimal totalConEnvio = resumen.subtotal().add(resumen.igv()).add(cupon.costoEnvioFinal())
                .subtract(descuento)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        pedido.setPromocionId(null);
        pedido.setDescuento(descuento);
        pedido.setCostoEnvio(cupon.costoEnvioFinal());
        pedido.setTotal(totalConEnvio);
        pedido.setFechaActualizacion(LocalDateTime.now());
        pedidoRepository.save(pedido);

        int montoCentimos = totalConEnvio.multiply(BigDecimal.valueOf(100)).intValue();
        String email = req.email() != null ? req.email() : "cliente@casachantilly.pe";

        PagoGateway.ResultadoCargo cargo = pagoGateway.crearCargo(req.tokenTarjeta(), montoCentimos, email);
        boolean aprobado = cargo.aprobado();
        String referencia = cargo.referencia();
        String mensaje = cargo.mensaje();

        Pago pago = new Pago();
        pago.setPedidoId(pedido.getId());
        pago.setMetodoPagoId(req.metodoPagoId());
        pago.setMonto(totalConEnvio);
        pago.setMoneda("PEN");
        pago.setReferenciaExterna(referencia);
        pago.setIdTransaccionExterna(referencia);
        pago.setIdempotencyKey(idemKey);
        pago.setCodigoErrorProveedor(cargo.codigoError());

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
            hist.setObservacion("Pago aprobado - ref: " + referencia);
            historialRepository.save(hist);

            pedido.setEstadoActualId(estadoConfirmado.getId());
            pedido.setFechaActualizacion(LocalDateTime.now());
            pedidoRepository.save(pedido);

            carritoService.vaciarCarrito(req.carritoId());
            notificacionService.registrarEventoPedido(
                    usuarioId,
                    pedido.getId(),
                    "Pedido confirmado",
                    "Tu pedido " + (pedido.getCodigoPedido() != null ? pedido.getCodigoPedido() : "#" + pedido.getId())
                            + " fue confirmado exitosamente."
            );

            return new PagoResultDTO(
                    true,
                    mensaje,
                    pedido.getId(),
                    pago.getId(),
                    referencia,
                    totalConEnvio,
                    1,
                    pago.getEstado().name(),
                    pago.getIdempotencyKey(),
                    null
            );
        }

        pago.setEstado(Pago.EstadoPago.RECHAZADO);
        pago.setIntentos(1);
        pago.setProximoIntento(LocalDateTime.now().plusMinutes(5));
        pagoRepository.save(pago);

        return new PagoResultDTO(
                false,
                mensaje,
                pedido.getId(),
                pago.getId(),
                referencia,
                totalConEnvio,
                1,
                pago.getEstado().name(),
                pago.getIdempotencyKey(),
                pago.getCodigoErrorProveedor() == null ? "PAGO_RECHAZADO" : pago.getCodigoErrorProveedor()
        );
    }

    private CuponResultDTO resolverCupon(String codigoCupon, BigDecimal subtotal, BigDecimal costoEnvioCalculado) {
        if (codigoCupon == null || codigoCupon.isBlank()) {
            return new CuponResultDTO(
                    false,
                    "Sin cupon",
                    BigDecimal.ZERO,
                    null,
                    null,
                    BigDecimal.ZERO,
                    false,
                    costoEnvioCalculado,
                    costoEnvioCalculado
            );
        }
        return promocionService.validarCupon(codigoCupon, subtotal, costoEnvioCalculado);
    }

    private BigDecimal resolverCostoEnvio(Integer usuarioId, Integer direccionId, Boolean esRecojoTienda, String zonaEntrega) {
        if (Boolean.TRUE.equals(esRecojoTienda)) {
            return BigDecimal.ZERO;
        }

        if (direccionId == null) {
            throw new BadRequestException("Debes seleccionar una direccion de entrega");
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
            throw new BadRequestException("Debes seleccionar una direccion de entrega");
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
        if (franjaHorariaRepository.findById(franjaHorariaId).isEmpty()) {
            return false;
        }
        long pedidosEnFranja = pedidoRepository.countPedidosActivosPorFranja(franjaHorariaId);
        return pedidosEnFranja < CUPO_MAXIMO_FRANJA;
    }

    private void validarCapacidadOperativa(Integer franjaHorariaId) {
        var franja = franjaHorariaRepository.findById(franjaHorariaId)
                .orElseThrow(() -> new ResourceNotFoundException("Franja horaria no encontrada"));
        if (!capacidadProduccionService.hayCapacidadParaFecha(franja.getFecha())) {
            throw new BadRequestException("No hay capacidad de produccion disponible para la fecha seleccionada");
        }
    }

    private void validarCompatibilidadFranja(Integer franjaHorariaId, boolean recojo) {
        FranjaHoraria franja = franjaHorariaRepository.findById(franjaHorariaId)
                .orElseThrow(() -> new ResourceNotFoundException("Franja horaria no encontrada"));
        if (recojo && FranjaHoraria.TipoFranja.DELIVERY.equals(franja.getTipo())) {
            throw new BadRequestException("La franja seleccionada solo permite delivery");
        }
        if (!recojo && FranjaHoraria.TipoFranja.RECOJO.equals(franja.getTipo())) {
            throw new BadRequestException("La franja seleccionada solo permite recojo en tienda");
        }
    }

    private BigDecimal obtenerRecargoUrgencia(boolean recojo) {
        if (recojo) {
            throw new BadRequestException("La urgencia aplica solo para delivery");
        }
        if (recargoUrgencia == null || recargoUrgencia.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return recargoUrgencia.setScale(2, RoundingMode.HALF_UP);
    }

    private void validarStockDisponible(Iterable<CarritoItemDTO> items) {
        for (CarritoItemDTO item : items) {
            ProductoVariante variante = varianteRepository.findByIdForUpdate(item.varianteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Variante no encontrada: " + item.varianteId()));
            if (!Boolean.TRUE.equals(variante.getActivo())) {
                throw new BadRequestException("La variante '" + item.varianteNombre() + "' no esta activa");
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

    private String normalizarIdempotencyKey(String key) {
        if (key == null) {
            return null;
        }
        String clean = key.trim();
        if (clean.isBlank()) {
            return null;
        }
        if (clean.length() > 80) {
            throw new BadRequestException("idempotencyKey excede longitud maxima de 80");
        }
        return clean;
    }
}