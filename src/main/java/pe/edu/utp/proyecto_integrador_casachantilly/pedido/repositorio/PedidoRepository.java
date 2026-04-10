package pe.edu.utp.proyecto_integrador_casachantilly.pedido.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.entidad.Pedido;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    List<Pedido> findByUsuarioIdOrderByFechaCreacionDesc(Integer usuarioId);
    List<Pedido> findAllByOrderByFechaCreacionDesc();
    Optional<Pedido> findByIdAndUsuarioId(Integer id, Integer usuarioId);
    boolean existsByIdAndUsuarioId(Integer id, Integer usuarioId);
    @Query(value = """
            select count(*)
              from pedido p
             where p.franja_horaria_id = :franjaHorariaId
               and p.estado_actual_id not in (7,8)
            """, nativeQuery = true)
    long countPedidosActivosPorFranja(Integer franjaHorariaId);

    @Query(value = """
            select count(*)
              from pedido p
              join franja_horaria f on f.id = p.franja_horaria_id
             where f.fecha = :fecha
               and p.estado_actual_id in (1,2,3,4,5)
            """, nativeQuery = true)
    long countPedidosOperativosPorFecha(LocalDate fecha);

    @Query(value = """
            select count(*)
              from pedido p
             where p.estado_actual_id in (1,2)
               and p.fecha_creacion >= :desde
            """, nativeQuery = true)
    long countPedidosNuevosPendientes(LocalDateTime desde);
}
