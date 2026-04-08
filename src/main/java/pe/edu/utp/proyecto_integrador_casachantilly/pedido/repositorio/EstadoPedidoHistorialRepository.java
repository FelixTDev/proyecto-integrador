package pe.edu.utp.proyecto_integrador_casachantilly.pedido.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.entidad.EstadoPedidoHistorial;

import java.util.List;

@Repository
public interface EstadoPedidoHistorialRepository extends JpaRepository<EstadoPedidoHistorial, Integer> {
    List<EstadoPedidoHistorial> findByPedidoIdOrderByFechaAsc(Integer pedidoId);
}
