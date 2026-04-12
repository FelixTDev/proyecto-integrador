package pe.edu.utp.proyecto_integrador_casachantilly.pedido.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.entidad.PedidoValidacionAuditoria;

import java.util.List;

@Repository
public interface PedidoValidacionAuditoriaRepository extends JpaRepository<PedidoValidacionAuditoria, Integer> {
    List<PedidoValidacionAuditoria> findByPedidoIdOrderByFechaDesc(Integer pedidoId);
}
