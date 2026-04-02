package pe.edu.utp.proyecto_integrador_casachantilly.pedido.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.entidad.Pago;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Integer> {
}
