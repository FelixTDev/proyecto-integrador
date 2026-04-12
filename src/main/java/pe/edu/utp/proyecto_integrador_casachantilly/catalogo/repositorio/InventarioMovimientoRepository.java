package pe.edu.utp.proyecto_integrador_casachantilly.catalogo.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.entidad.InventarioMovimiento;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventarioMovimientoRepository extends JpaRepository<InventarioMovimiento, Integer> {
    List<InventarioMovimiento> findByVarianteIdOrderByFechaDesc(Integer varianteId);
    List<InventarioMovimiento> findByVarianteIdAndFechaBetweenOrderByFechaDesc(
            Integer varianteId,
            LocalDateTime desde,
            LocalDateTime hasta
    );
}
