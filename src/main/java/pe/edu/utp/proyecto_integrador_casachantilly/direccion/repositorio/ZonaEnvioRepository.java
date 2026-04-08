package pe.edu.utp.proyecto_integrador_casachantilly.direccion.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.utp.proyecto_integrador_casachantilly.direccion.entidad.ZonaEnvio;

import java.util.List;
import java.util.Optional;

@Repository
public interface ZonaEnvioRepository extends JpaRepository<ZonaEnvio, Integer> {
    List<ZonaEnvio> findByActivoTrueOrderByNombreDistritoAsc();
    Optional<ZonaEnvio> findByIdAndActivoTrue(Integer id);
}
