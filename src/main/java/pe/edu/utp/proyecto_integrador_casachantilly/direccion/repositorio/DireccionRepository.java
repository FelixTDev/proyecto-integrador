package pe.edu.utp.proyecto_integrador_casachantilly.direccion.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.utp.proyecto_integrador_casachantilly.direccion.entidad.Direccion;

import java.util.List;
import java.util.Optional;

@Repository
public interface DireccionRepository extends JpaRepository<Direccion, Integer> {
    List<Direccion> findByUsuarioIdOrderByEsPrincipalDescIdDesc(Integer usuarioId);
    List<Direccion> findByUsuarioIdAndActivoTrueOrderByEsPrincipalDescIdDesc(Integer usuarioId);
    Optional<Direccion> findByIdAndUsuarioId(Integer id, Integer usuarioId);
    Optional<Direccion> findFirstByUsuarioIdAndEsPrincipalTrueAndActivoTrue(Integer usuarioId);
}
