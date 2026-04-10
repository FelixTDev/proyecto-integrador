package pe.edu.utp.proyecto_integrador_casachantilly.fidelidad.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;
import pe.edu.utp.proyecto_integrador_casachantilly.fidelidad.entidad.PuntosFidelidad;

import java.util.List;

@Repository
public interface PuntosFidelidadRepository extends JpaRepository<PuntosFidelidad, Integer> {

    @Query("SELECT COALESCE(SUM(p.puntos), 0) FROM PuntosFidelidad p WHERE p.usuarioId = :usuarioId")
    int obtenerSaldo(Integer usuarioId);

    List<PuntosFidelidad> findByUsuarioIdOrderByFechaDesc(Integer usuarioId);

    boolean existsByPedidoIdAndTipo(Integer pedidoId, PuntosFidelidad.TipoPuntos tipo);
}
