package pe.edu.utp.proyecto_integrador_casachantilly.auth.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.entidad.Sesion;

import java.util.Optional;

@Repository
public interface SesionRepository extends JpaRepository<Sesion, Integer> {
    Optional<Sesion> findByTokenHashAndActivoTrue(String tokenHash);
}
