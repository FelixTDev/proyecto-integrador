package pe.edu.utp.proyecto_integrador_casachantilly.notificacion.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.entidad.Usuario;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.repositorio.UsuarioRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.ResourceNotFoundException;
import pe.edu.utp.proyecto_integrador_casachantilly.notificacion.dto.NotificacionDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.notificacion.entidad.Notificacion;
import pe.edu.utp.proyecto_integrador_casachantilly.notificacion.repositorio.NotificacionRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificacionService {

    @Autowired private NotificacionRepository notificacionRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    @Transactional
    public void registrarEventoPedido(Integer usuarioId, Integer pedidoId, String asunto, String mensaje) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado para notificación"));

        Notificacion n = new Notificacion();
        n.setUsuarioId(usuarioId);
        n.setPedidoId(pedidoId);
        n.setCanal(Notificacion.Canal.EMAIL);
        n.setAsunto(asunto);
        n.setMensaje(mensaje);
        n.setLeida(false);
        n.setIntentos(1);
        n.setFechaEnvio(LocalDateTime.now());

        String destino = usuario.getEmail();
        n.setDestinoCanal(destino);
        if (destino == null || destino.isBlank()) {
            n.setEstadoEnvio(Notificacion.EstadoEnvio.ERROR);
        } else {

            n.setEstadoEnvio(Notificacion.EstadoEnvio.ENVIADA);
        }
        notificacionRepository.save(n);
    }

    @Transactional(readOnly = true)
    public List<NotificacionDTO> listarNotificacionesUsuario(Integer usuarioId) {
        return notificacionRepository.findTop20ByUsuarioIdOrderByFechaEnvioDesc(usuarioId).stream()
                .map(this::toDto)
                .toList();
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
}
