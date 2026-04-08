package pe.edu.utp.proyecto_integrador_casachantilly.pedido.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.entidad.Pedido;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    List<Pedido> findByUsuarioIdOrderByFechaCreacionDesc(Integer usuarioId);
    List<Pedido> findAllByOrderByFechaCreacionDesc();
    Optional<Pedido> findByIdAndUsuarioId(Integer id, Integer usuarioId);
    long countByFranjaHorariaIdAndFechaCreacionBetween(Integer franjaHorariaId, LocalDateTime inicio, LocalDateTime fin);
}
