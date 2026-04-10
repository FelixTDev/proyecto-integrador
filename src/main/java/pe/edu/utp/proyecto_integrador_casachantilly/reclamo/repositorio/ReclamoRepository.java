package pe.edu.utp.proyecto_integrador_casachantilly.reclamo.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.utp.proyecto_integrador_casachantilly.reclamo.entidad.Reclamo;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReclamoRepository extends JpaRepository<Reclamo, Integer> {
    List<Reclamo> findByUsuarioIdOrderByFechaCreacionDesc(Integer usuarioId);
    List<Reclamo> findAllByOrderByFechaCreacionDesc();
    Optional<Reclamo> findByIdAndUsuarioId(Integer id, Integer usuarioId);
    long countByEstado(Reclamo.EstadoReclamo estado);
}
