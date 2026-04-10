package pe.edu.utp.proyecto_integrador_casachantilly.promocion.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.edu.utp.proyecto_integrador_casachantilly.promocion.entidad.Promocion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromocionRepository extends JpaRepository<Promocion, Integer> {

    Optional<Promocion> findByCodigoCuponIgnoreCase(String codigoCupon);

    @Query("SELECT p FROM Promocion p WHERE p.activo = true " +
           "AND (p.fechaInicio IS NULL OR p.fechaInicio <= :ahora) " +
           "AND (p.fechaFin IS NULL OR p.fechaFin >= :ahora)")
    List<Promocion> findVigentes(LocalDateTime ahora);

    List<Promocion> findAllByOrderByActivoDescFechaFinAsc();
}
