package pe.edu.utp.proyecto_integrador_casachantilly.pedido.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.entidad.EstadoPedido;

import java.util.Optional;

@Repository
public interface EstadoPedidoRepository extends JpaRepository<EstadoPedido, Integer> {
    Optional<EstadoPedido> findByOrden(Integer orden);
}
