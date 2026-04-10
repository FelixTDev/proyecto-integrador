package pe.edu.utp.proyecto_integrador_casachantilly.personalizacion.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.utp.proyecto_integrador_casachantilly.personalizacion.entidad.Personalizacion;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonalizacionRepository extends JpaRepository<Personalizacion, Integer> {
    Optional<Personalizacion> findByDetallePedidoId(Integer detallePedidoId);
    List<Personalizacion> findByDetallePedidoIdIn(List<Integer> detalleIds);
}
