package pe.edu.utp.proyecto_integrador_casachantilly.chat.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.entidad.Usuario;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.repositorio.UsuarioRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.chat.dto.ChatMensajeDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.chat.entidad.ChatMensaje;
import pe.edu.utp.proyecto_integrador_casachantilly.chat.repositorio.ChatMensajeRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.BadRequestException;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.ResourceNotFoundException;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.entidad.Pedido;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.repositorio.PedidoRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired private ChatMensajeRepository chatRepo;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    @Transactional
    public ChatMensajeDTO enviarMensaje(Integer pedidoId, Integer usuarioId, boolean esStaff, String mensaje) {
        validarAccesoPedido(pedidoId, usuarioId, esStaff);
        if (mensaje == null || mensaje.trim().isEmpty()) {
            throw new BadRequestException("El mensaje no puede estar vacio");
        }

        ChatMensaje cm = new ChatMensaje();
        cm.setPedidoId(pedidoId);
        cm.setUsuarioId(usuarioId);
        cm.setMensaje(mensaje.trim());
        chatRepo.save(cm);
        return toDto(cm, usuarioId);
    }

    @Transactional
    public List<ChatMensajeDTO> listarMensajes(Integer pedidoId, Integer usuarioId, boolean esStaff) {
        validarAccesoPedido(pedidoId, usuarioId, esStaff);
        chatRepo.marcarLeidosPorPedido(pedidoId, usuarioId);
        List<ChatMensaje> mensajes = chatRepo.findByPedidoIdOrderByFechaAsc(pedidoId);

        List<Integer> userIds = mensajes.stream().map(ChatMensaje::getUsuarioId).distinct().toList();
        Map<Integer, String> nombres = usuarioRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(Usuario::getId, Usuario::getNombre));

        return mensajes.stream().map(m -> {
            String nombre = nombres.getOrDefault(m.getUsuarioId(), "Usuario");
            return new ChatMensajeDTO(m.getId(), m.getPedidoId(), m.getUsuarioId(),
                    nombre, m.getMensaje(), m.getLeido(), m.getFecha(),
                    m.getUsuarioId().equals(usuarioId));
        }).toList();
    }

    @Transactional(readOnly = true)
    public long contarNoLeidosCliente(Integer usuarioId) {
        return chatRepo.contarNoLeidosCliente(usuarioId);
    }

    @Transactional(readOnly = true)
    public long contarNoLeidosAdmin() {
        return chatRepo.contarNoLeidosAdmin();
    }

    private ChatMensajeDTO toDto(ChatMensaje m, Integer currentUserId) {
        String nombre = usuarioRepository.findById(m.getUsuarioId())
                .map(Usuario::getNombre).orElse("Usuario");
        return new ChatMensajeDTO(m.getId(), m.getPedidoId(), m.getUsuarioId(),
                nombre, m.getMensaje(), m.getLeido(), m.getFecha(),
                m.getUsuarioId().equals(currentUserId));
    }

    private void validarAccesoPedido(Integer pedidoId, Integer usuarioId, boolean esStaff) {
        if (esStaff) {
            pedidoRepository.findById(pedidoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
            return;
        }
        boolean permitido = pedidoRepository.existsByIdAndUsuarioId(pedidoId, usuarioId);
        if (!permitido) {
            throw new ResourceNotFoundException("Pedido no encontrado para el usuario");
        }
    }
}
