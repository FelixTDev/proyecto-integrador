package pe.edu.utp.proyecto_integrador_casachantilly.entrega.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.edu.utp.proyecto_integrador_casachantilly.entrega.entidad.FranjaHoraria;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FranjaHorariaRepository extends JpaRepository<FranjaHoraria, Integer> {

    @Query("SELECT f FROM FranjaHoraria f WHERE f.fecha = :fecha AND f.cuposOcupados < f.cuposTotales ORDER BY f.horaInicio")
    List<FranjaHoraria> findDisponiblesByFecha(LocalDate fecha);

    List<FranjaHoraria> findByFechaOrderByHoraInicio(LocalDate fecha);

    @Query("SELECT f FROM FranjaHoraria f WHERE f.fecha >= :desde ORDER BY f.fecha, f.horaInicio")
    List<FranjaHoraria> findFuturas(LocalDate desde);
}
