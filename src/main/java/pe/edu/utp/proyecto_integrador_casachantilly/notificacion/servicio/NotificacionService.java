package pe.edu.utp.proyecto_integrador_casachantilly.notificacion.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.entidad.Usuario;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.repositorio.UsuarioRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.ResourceNotFoundException;
import pe.edu.utp.proyecto_integrador_casachantilly.notificacion.dto.NotificacionDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.notificacion.entidad.Notificacion;
import pe.edu.utp.proyecto_integrador_casachantilly.notificacion.repositorio.NotificacionRepository;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificacionService {

    @Autowired private NotificacionRepository notificacionRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Value("${app.notificacion.canal-preferido:AUTO}")
    private String canalPreferido;
    private final Map<Notificacion.Canal, NotificacionCanalGateway> gateways = new EnumMap<>(Notificacion.Canal.class);

    public NotificacionService(List<NotificacionCanalGateway> gatewayList) {
        for (NotificacionCanalGateway g : gatewayList) {
            gateways.put(g.getCanal(), g);
        }
    }

    @Transactional
    public void registrarEventoPedido(Integer usuarioId, Integer pedidoId, String asunto, String mensaje) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado para notificación"));

        Notificacion.Canal canal = resolverCanal(usuario);
        String destino = resolverDestino(canal, usuario);
        NotificacionCanalGateway gateway = gateways.get(canal);
        boolean enviado = gateway != null && gateway.enviar(destino, asunto, mensaje);

        Notificacion n = new Notificacion();
        n.setUsuarioId(usuarioId);
        n.setPedidoId(pedidoId);
        n.setCanal(canal);
        n.setAsunto(asunto);
        n.setMensaje(mensaje);
        n.setLeida(false);
        n.setIntentos(1);
        n.setFechaEnvio(LocalDateTime.now());
        n.setDestinoCanal(destino);
        n.setEstadoEnvio(enviado ? Notificacion.EstadoEnvio.ENVIADA : Notificacion.EstadoEnvio.ERROR);
        notificacionRepository.save(n);
    }

    @Transactional(readOnly = true)
    public List<NotificacionDTO> listarNotificacionesUsuario(Integer usuarioId) {
        return notificacionRepository.findTop20ByUsuarioIdOrderByFechaEnvioDesc(usuarioId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void marcarLeida(Integer notificacionId, Integer usuarioId) {
        Notificacion n = notificacionRepository.findByIdAndUsuarioId(notificacionId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada"));
        n.setLeida(true);
        notificacionRepository.save(n);
    }

    @Transactional(readOnly = true)
    public long contarNoLeidas(Integer usuarioId) {
        return notificacionRepository.countByUsuarioIdAndLeidaFalse(usuarioId);
    }

    private NotificacionDTO toDto(Notificacion n) {
        return new NotificacionDTO(
                n.getId(),
                n.getUsuarioId(),
                n.getPedidoId(),
                n.getCanal().name(),
                n.getAsunto(),
                n.getMensaje(),
                n.getLeida(),
                n.getIntentos(),
                n.getEstadoEnvio().name(),
                n.getDestinoCanal(),
                n.getFechaEnvio()
        );
    }

    private Notificacion.Canal resolverCanal(Usuario usuario) {
        String preferido = canalPreferido == null ? "AUTO" : canalPreferido.trim().toUpperCase();
        if ("WHATSAPP".equals(preferido) && usuario.getTelefono() != null && !usuario.getTelefono().isBlank()) {
            return Notificacion.Canal.WHATSAPP;
        }
        if ("EMAIL".equals(preferido)) {
            return Notificacion.Canal.EMAIL;
        }
        if (usuario.getTelefono() != null && !usuario.getTelefono().isBlank()) {
            return Notificacion.Canal.WHATSAPP;
        }
        return Notificacion.Canal.EMAIL;
    }

    private String resolverDestino(Notificacion.Canal canal, Usuario usuario) {
        return switch (canal) {
            case WHATSAPP, SMS -> usuario.getTelefono();
            case EMAIL, PUSH -> usuario.getEmail();
        };
    }
}
