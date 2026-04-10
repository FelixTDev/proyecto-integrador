package pe.edu.utp.proyecto_integrador_casachantilly.chat.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.edu.utp.proyecto_integrador_casachantilly.chat.entidad.ChatMensaje;

import java.util.List;

@Repository
public interface ChatMensajeRepository extends JpaRepository<ChatMensaje, Integer> {

    List<ChatMensaje> findByPedidoIdOrderByFechaAsc(Integer pedidoId);

    @Modifying
    @Query("UPDATE ChatMensaje c SET c.leido = true WHERE c.pedidoId = :pedidoId AND c.usuarioId <> :usuarioId AND c.leido = false")
    int marcarLeidosPorPedido(Integer pedidoId, Integer usuarioId);

    @Query("SELECT COUNT(c) FROM ChatMensaje c WHERE c.pedidoId IN " +
           "(SELECT p.id FROM pe.edu.utp.proyecto_integrador_casachantilly.pedido.entidad.Pedido p WHERE p.usuarioId = :usuarioId) " +
           "AND c.usuarioId <> :usuarioId AND c.leido = false")
    long contarNoLeidosCliente(Integer usuarioId);

    @Query("SELECT COUNT(c) FROM ChatMensaje c WHERE c.leido = false " +
           "AND c.usuarioId NOT IN (SELECT u.id FROM pe.edu.utp.proyecto_integrador_casachantilly.auth.entidad.Usuario u " +
           "JOIN u.roles r WHERE r.nombre = 'ADMIN' OR r.nombre = 'VENDEDOR')")
    long contarNoLeidosAdmin();
}
