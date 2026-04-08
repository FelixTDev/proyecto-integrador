package pe.edu.utp.proyecto_integrador_casachantilly.notificacion.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.utp.proyecto_integrador_casachantilly.notificacion.entidad.Notificacion;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {
    List<Notificacion> findTop20ByUsuarioIdOrderByFechaEnvioDesc(Integer usuarioId);
}
