package pe.edu.utp.proyecto_integrador_casachantilly.reclamo.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.entidad.Usuario;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.repositorio.UsuarioRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.BadRequestException;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.ResourceNotFoundException;
import pe.edu.utp.proyecto_integrador_casachantilly.notificacion.servicio.NotificacionService;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.entidad.Pedido;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.repositorio.PedidoRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.reclamo.dto.ReclamoDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.reclamo.dto.ReclamoEstadoRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.reclamo.dto.ReclamoRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.reclamo.entidad.Reclamo;
import pe.edu.utp.proyecto_integrador_casachantilly.reclamo.repositorio.ReclamoRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReclamoService {

    @Autowired private ReclamoRepository reclamoRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private NotificacionService notificacionService;

    @Transactional
    public ReclamoDTO crearReclamo(Integer usuarioId, ReclamoRequest req) {
        Pedido pedido = pedidoRepository.findByIdAndUsuarioId(req.pedidoId(), usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        Reclamo r = new Reclamo();
        r.setPedidoId(pedido.getId());
        r.setUsuarioId(usuarioId);
        r.setTipo(parseTipo(req.tipo()));
        r.setDescripcion(req.descripcion().trim());
        reclamoRepository.save(r);

        notificacionService.registrarEventoPedido(usuarioId, pedido.getId(),
                "Reclamo registrado", "Tu reclamo #" + r.getId() + " ha sido registrado y esta en revision.");
        return toDto(r);
    }

    @Transactional(readOnly = true)
    public List<ReclamoDTO> listarPorCliente(Integer usuarioId) {
        return reclamoRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ReclamoDTO> listarTodos() {
        return reclamoRepository.findAllByOrderByFechaCreacionDesc()
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public ReclamoDTO actualizarEstado(Integer reclamoId, ReclamoEstadoRequest req) {
        Reclamo r = reclamoRepository.findById(reclamoId)
                .orElseThrow(() -> new ResourceNotFoundException("Reclamo no encontrado: " + reclamoId));

        if (req.estado() != null) {
            r.setEstado(parseEstado(req.estado()));
            if (r.getEstado() == Reclamo.EstadoReclamo.RESUELTO || r.getEstado() == Reclamo.EstadoReclamo.CERRADO) {
                r.setFechaResolucion(LocalDateTime.now());
            }
        }
        if (req.detalleResolucion() != null) {
            r.setDetalleResolucion(req.detalleResolucion().trim());
        }
        if (req.montoReembolso() != null) {
            r.setMontoReembolso(req.montoReembolso());
        }
        r.setFechaActualizacion(LocalDateTime.now());
        reclamoRepository.save(r);

        notificacionService.registrarEventoPedido(r.getUsuarioId(), r.getPedidoId(),
                "Reclamo actualizado", "Tu reclamo #" + r.getId() + " cambio a estado: " + r.getEstado().name());
        return toDto(r);
    }

    private ReclamoDTO toDto(Reclamo r) {
        String nombreCliente = usuarioRepository.findById(r.getUsuarioId())
                .map(Usuario::getNombre).orElse("-");
        String codigoPedido = pedidoRepository.findById(r.getPedidoId())
                .map(Pedido::getCodigoPedido).orElse("-");

        return new ReclamoDTO(r.getId(), r.getPedidoId(), codigoPedido,
                r.getUsuarioId(), nombreCliente, r.getTipo().name(),
                r.getDescripcion(), r.getEstado().name(),
                r.getMontoReembolso(), r.getPrioridad().name(),
                r.getFechaCreacion(), r.getFechaResolucion(), r.getDetalleResolucion());
    }

    private Reclamo.TipoReclamo parseTipo(String tipoRaw) {
        if (tipoRaw == null || tipoRaw.isBlank()) {
            throw new BadRequestException("El tipo de reclamo es obligatorio");
        }
        String normalizado = tipoRaw.trim().toUpperCase();
        if ("CALIDAD".equals(normalizado)) {
            return Reclamo.TipoReclamo.QUEJA;
        }
        try {
            return Reclamo.TipoReclamo.valueOf(normalizado);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Tipo de reclamo invalido. Valores: REEMBOLSO, REPOSICION, QUEJA, CALIDAD");
        }
    }

    private Reclamo.EstadoReclamo parseEstado(String estadoRaw) {
        if (estadoRaw == null || estadoRaw.isBlank()) {
            throw new BadRequestException("El estado del reclamo es obligatorio");
        }
        try {
            return Reclamo.EstadoReclamo.valueOf(estadoRaw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Estado invalido. Valores: ABIERTO, EN_REVISION, RESUELTO, CERRADO");
        }
    }
}
