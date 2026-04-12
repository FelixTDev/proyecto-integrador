package pe.edu.utp.proyecto_integrador_casachantilly.pedido.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.entidad.Usuario;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.repositorio.UsuarioRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.carrito.entidad.Carrito;
import pe.edu.utp.proyecto_integrador_casachantilly.carrito.servicio.CarritoService;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.entidad.ProductoVariante;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.repositorio.ProductoVarianteRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.BadRequestException;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.ResourceNotFoundException;
import pe.edu.utp.proyecto_integrador_casachantilly.notificacion.servicio.NotificacionService;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.dto.*;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.entidad.DetallePedido;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.entidad.EstadoPedido;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.entidad.EstadoPedidoHistorial;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.entidad.Pedido;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.entidad.PedidoValidacionAuditoria;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.repositorio.EstadoPedidoHistorialRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.repositorio.EstadoPedidoRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.repositorio.PedidoRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.repositorio.PedidoValidacionAuditoriaRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PedidoGestionService {

    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private EstadoPedidoRepository estadoPedidoRepository;
    @Autowired private EstadoPedidoHistorialRepository historialRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private CarritoService carritoService;
    @Autowired private ProductoVarianteRepository varianteRepository;
    @Autowired private NotificacionService notificacionService;
    @Autowired private PedidoValidacionAuditoriaRepository pedidoValidacionAuditoriaRepository;

    @Transactional(readOnly = true)
    public List<AdminPedidoDTO> listarPedidosAdmin() {
        List<Pedido> pedidos = pedidoRepository.findAllByOrderByFechaCreacionDesc();
        Map<Integer, Usuario> usuarios = usuarioRepository.findAllById(
                pedidos.stream().map(Pedido::getUsuarioId).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(Usuario::getId, u -> u));

        return pedidos.stream().map(p -> {
            EstadoPedido estado = obtenerEstadoActual(p);
            Usuario usuario = usuarios.get(p.getUsuarioId());
            return new AdminPedidoDTO(
                    p.getId(),
                    p.getCodigoPedido(),
                    p.getUsuarioId(),
                    usuario != null ? usuario.getNombre() : "—",
                    usuario != null ? usuario.getEmail() : "—",
                    p.getTotal(),
                    p.getFechaCreacion(),
                    estado.getId(),
                    estado.getNombre()
            );
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoHistorialDTO> listarHistorialCliente(Integer usuarioId) {
        List<Pedido> pedidos = pedidoRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
        return pedidos.stream().map(this::toHistorialDto).toList();
    }

    @Transactional
    public AdminPedidoDTO validarPedido(Integer pedidoId, AdminPedidoValidacionRequest req, Integer adminId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + pedidoId));

        EstadoPedido actual = obtenerEstadoActual(pedido);
        if (!Objects.equals(actual.getOrden(), 1)) {
            throw new BadRequestException("Solo se pueden validar pedidos en estado 'Pendiente de pago'");
        }

        EstadoPedido siguiente;
        String observacion;
        if (Boolean.TRUE.equals(req.aprobar())) {
            siguiente = estadoPedidoRepository.findByOrden(2)
                    .orElseThrow(() -> new ResourceNotFoundException("Estado 'Pago confirmado' no encontrado"));
            observacion = (req.motivo() == null || req.motivo().isBlank())
                    ? "Pedido aprobado por validación administrativa"
                    : req.motivo().trim();
        } else {
            if (req.motivo() == null || req.motivo().isBlank()) {
                throw new BadRequestException("El motivo es obligatorio cuando se rechaza un pedido");
            }
            siguiente = estadoPedidoRepository.findByOrden(8)
                    .orElseThrow(() -> new ResourceNotFoundException("Estado 'Rechazado' no encontrado"));
            observacion = "Rechazado: " + req.motivo().trim();
        }

        aplicarCambioEstado(pedido, siguiente, observacion, adminId);
        registrarAuditoriaValidacion(pedidoId, adminId, Boolean.TRUE.equals(req.aprobar()), req.motivo());
        return toAdminPedidoDTO(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoValidacionAuditoriaDTO> listarAuditoriaValidacion(Integer pedidoId) {
        pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + pedidoId));
        return pedidoValidacionAuditoriaRepository.findByPedidoIdOrderByFechaDesc(pedidoId).stream()
                .map(this::toValidacionDto)
                .toList();
    }

    @Transactional
    public AdminPedidoDTO cambiarEstado(Integer pedidoId, AdminPedidoEstadoRequest req, Integer adminId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + pedidoId));
        EstadoPedido actual = obtenerEstadoActual(pedido);

        EstadoPedido nuevo = estadoPedidoRepository.findById(req.estadoId())
                .orElseThrow(() -> new ResourceNotFoundException("Estado no encontrado: " + req.estadoId()));

        validarTransicion(actual.getOrden(), nuevo.getOrden());

        String observacion = (req.observacion() == null || req.observacion().isBlank())
                ? "Cambio de estado administrativo"
                : req.observacion().trim();

        aplicarCambioEstado(pedido, nuevo, observacion, adminId);
        return toAdminPedidoDTO(pedido);
    }

    @Transactional(readOnly = true)
    public ComprobantePedidoDTO obtenerComprobante(Integer pedidoId, Integer usuarioId, boolean isAdmin) {
        Pedido pedido = isAdmin
                ? pedidoRepository.findById(pedidoId).orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"))
                : pedidoRepository.findByIdAndUsuarioId(pedidoId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado para el usuario"));

        EstadoPedido actual = obtenerEstadoActual(pedido);

        List<ComprobanteItemDTO> items = pedido.getDetalles().stream().map(this::toComprobanteItem).toList();

        return new ComprobantePedidoDTO(
                pedido.getId(),
                pedido.getCodigoPedido(),
                pedido.getUsuarioId(),
                pedido.getDireccionId(),
                pedido.getEsRecojoTienda(),
                actual.getNombre(),
                pedido.getFechaCreacion(),
                pedido.getSubtotal(),
                pedido.getDescuento(),
                pedido.getCostoEnvio(),
                pedido.getImpuestos(),
                pedido.getTotal(),
                items
        );
    }

    @Transactional
    public ReordenarResultadoDTO reordenarPedido(Integer pedidoId, Integer usuarioId, ReordenarRequestDTO req) {
        Pedido pedido = pedidoRepository.findByIdAndUsuarioId(pedidoId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado para el usuario"));
        if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            throw new BadRequestException("El pedido no tiene ítems para reordenar");
        }

        Carrito carrito = carritoService.getCarritoActivo(usuarioId);
        if (req == null || !Boolean.FALSE.equals(req.limpiarCarrito())) {
            carritoService.vaciarCarrito(carrito.getId());
        }

        List<ReordenarItemResultadoDTO> resultados = new ArrayList<>();
        int totalSolicitados = 0;
        int totalAgregados = 0;
        int totalRechazados = 0;

        for (DetallePedido d : pedido.getDetalles()) {
            int solicitada = d.getCantidad() != null ? d.getCantidad() : 0;
            totalSolicitados += solicitada;

            var varianteOpt = varianteRepository.findById(d.getVarianteId());
            if (varianteOpt.isEmpty()) {
                totalRechazados += solicitada;
                resultados.add(new ReordenarItemResultadoDTO(
                        d.getVarianteId(), d.getNombreSnapshot(), solicitada, 0,
                        d.getPrecioUnitarioSnapshot(), null,
                        "RECHAZADO", "La variante ya no existe"));
                continue;
            }

            ProductoVariante v = varianteOpt.get();
            if (!Boolean.TRUE.equals(v.getActivo()) || v.getStockDisponible() == null || v.getStockDisponible() <= 0) {
                totalRechazados += solicitada;
                resultados.add(new ReordenarItemResultadoDTO(
                        v.getId(), d.getNombreSnapshot(), solicitada, 0,
                        d.getPrecioUnitarioSnapshot(), v.getPrecio(),
                        "RECHAZADO", "La variante no está disponible actualmente"));
                continue;
            }

            int cantidadAAgregar = Math.min(solicitada, v.getStockDisponible());
            if (cantidadAAgregar <= 0) {
                totalRechazados += solicitada;
                resultados.add(new ReordenarItemResultadoDTO(
                        v.getId(), d.getNombreSnapshot(), solicitada, 0,
                        d.getPrecioUnitarioSnapshot(), v.getPrecio(),
                        "RECHAZADO", "Sin stock disponible"));
                continue;
            }

            try {
                carritoService.addItem(carrito.getId(), v.getId(), cantidadAAgregar);
                totalAgregados += cantidadAAgregar;
                if (cantidadAAgregar < solicitada) {
                    totalRechazados += (solicitada - cantidadAAgregar);
                }

                String estado = cantidadAAgregar < solicitada ? "PARCIAL" : "AGREGADO";
                String msg = cantidadAAgregar < solicitada
                        ? "Stock parcial. Se agregaron " + cantidadAAgregar + " de " + solicitada
                        : "Ítem agregado al carrito";
                if (d.getPrecioUnitarioSnapshot() != null && v.getPrecio() != null
                        && d.getPrecioUnitarioSnapshot().compareTo(v.getPrecio()) != 0) {
                    msg += ". Precio actualizado al valor vigente";
                }

                resultados.add(new ReordenarItemResultadoDTO(
                        v.getId(), d.getNombreSnapshot(), solicitada, cantidadAAgregar,
                        d.getPrecioUnitarioSnapshot(), v.getPrecio(),
                        estado, msg));
            } catch (BadRequestException ex) {
                totalRechazados += solicitada;
                resultados.add(new ReordenarItemResultadoDTO(
                        v.getId(), d.getNombreSnapshot(), solicitada, 0,
                        d.getPrecioUnitarioSnapshot(), v.getPrecio(),
                        "RECHAZADO", ex.getMessage()));
            }
        }

        String mensaje = totalAgregados > 0
                ? "Reordenación procesada. Revisa los ajustes aplicados"
                : "No se pudo agregar ningún ítem del pedido anterior";

        return new ReordenarResultadoDTO(
                pedidoId,
                carrito.getId(),
                totalSolicitados,
                totalAgregados,
                totalRechazados,
                resultados,
                mensaje
        );
    }

    private ComprobanteItemDTO toComprobanteItem(DetallePedido d) {
        return new ComprobanteItemDTO(
                d.getId(),
                d.getVarianteId(),
                d.getNombreSnapshot(),
                d.getPrecioUnitarioSnapshot(),
                d.getCantidad(),
                d.getSubtotalLinea()
        );
    }

    private void aplicarCambioEstado(Pedido pedido, EstadoPedido nuevoEstado, String observacion, Integer usuarioId) {
        pedido.setEstadoActualId(nuevoEstado.getId());
        pedido.setFechaActualizacion(LocalDateTime.now());
        pedidoRepository.save(pedido);

        EstadoPedidoHistorial hist = new EstadoPedidoHistorial();
        hist.setPedidoId(pedido.getId());
        hist.setEstadoId(nuevoEstado.getId());
        hist.setUsuarioId(usuarioId);
        hist.setObservacion(observacion);
        historialRepository.save(hist);

        notificacionService.registrarEventoPedido(
                pedido.getUsuarioId(),
                pedido.getId(),
                "Estado de pedido actualizado",
                "Tu pedido " + (pedido.getCodigoPedido() != null ? pedido.getCodigoPedido() : "#" + pedido.getId())
                        + " cambió a estado: " + nuevoEstado.getNombre()
        );
    }

    private void validarTransicion(Integer ordenActual, Integer ordenNuevo) {
        if (ordenActual == null || ordenNuevo == null) {
            throw new BadRequestException("No se pudo validar la transición de estado");
        }

        Set<Integer> finales = Set.of(6, 7, 8);
        if (finales.contains(ordenActual)) {
            throw new BadRequestException("No se puede cambiar el estado de un pedido finalizado");
        }

        boolean esSiguienteSecuencial = Objects.equals(ordenNuevo, ordenActual + 1);
        boolean esCancelacionPermitida = Objects.equals(ordenNuevo, 7) && ordenActual < 6;

        if (!(esSiguienteSecuencial || esCancelacionPermitida)) {
            throw new BadRequestException("Transición no permitida desde el estado actual");
        }
    }

    private EstadoPedido obtenerEstadoActual(Pedido pedido) {
        if (pedido.getEstadoActualId() != null) {
            return estadoPedidoRepository.findById(pedido.getEstadoActualId())
                    .orElseThrow(() -> new ResourceNotFoundException("Estado actual no encontrado"));
        }
        return estadoPedidoRepository.findByOrden(1)
                .orElseThrow(() -> new ResourceNotFoundException("Estado inicial no encontrado"));
    }

    private AdminPedidoDTO toAdminPedidoDTO(Pedido p) {
        EstadoPedido estado = obtenerEstadoActual(p);
        Usuario usuario = usuarioRepository.findById(p.getUsuarioId()).orElse(null);
        return new AdminPedidoDTO(
                p.getId(),
                p.getCodigoPedido(),
                p.getUsuarioId(),
                usuario != null ? usuario.getNombre() : "—",
                usuario != null ? usuario.getEmail() : "—",
                p.getTotal(),
                p.getFechaCreacion(),
                estado.getId(),
                estado.getNombre()
        );
    }

    private PedidoHistorialDTO toHistorialDto(Pedido p) {
        EstadoPedido estado = obtenerEstadoActual(p);
        List<PedidoHistorialItemDTO> items = p.getDetalles().stream().map(d -> new PedidoHistorialItemDTO(
                d.getVarianteId(),
                d.getNombreSnapshot(),
                d.getCantidad(),
                d.getPrecioUnitarioSnapshot(),
                d.getSubtotalLinea()
        )).toList();

        return new PedidoHistorialDTO(
                p.getId(),
                p.getCodigoPedido(),
                p.getFechaCreacion(),
                estado.getId(),
                estado.getNombre(),
                p.getTotal(),
                p.getEsRecojoTienda(),
                p.getDireccionId(),
                items
        );
    }

    private void registrarAuditoriaValidacion(Integer pedidoId, Integer usuarioId, boolean aprobado, String motivo) {
        PedidoValidacionAuditoria auditoria = new PedidoValidacionAuditoria();
        auditoria.setPedidoId(pedidoId);
        auditoria.setUsuarioId(usuarioId);
        auditoria.setResultado(aprobado ? PedidoValidacionAuditoria.Resultado.APROBADO : PedidoValidacionAuditoria.Resultado.RECHAZADO);
        auditoria.setMotivo(motivo == null ? null : motivo.trim());
        pedidoValidacionAuditoriaRepository.save(auditoria);
    }

    private PedidoValidacionAuditoriaDTO toValidacionDto(PedidoValidacionAuditoria row) {
        Usuario usuario = usuarioRepository.findById(row.getUsuarioId()).orElse(null);
        return new PedidoValidacionAuditoriaDTO(
                row.getId(),
                row.getPedidoId(),
                row.getUsuarioId(),
                usuario == null ? null : usuario.getNombre(),
                row.getResultado().name(),
                row.getMotivo(),
                row.getFecha()
        );
    }
}
