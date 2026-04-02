package pe.edu.utp.proyecto_integrador_casachantilly.pedido.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.entidad.EstadoPedidoHistorial;

@Repository
public interface EstadoPedidoHistorialRepository extends JpaRepository<EstadoPedidoHistorial, Integer> {
}
