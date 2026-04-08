package pe.edu.utp.proyecto_integrador_casachantilly.auth.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.entidad.Sesion;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SesionRepository extends JpaRepository<Sesion, Integer> {
    Optional<Sesion> findByTokenHash(String tokenHash);
    Optional<Sesion> findByTokenHashAndActivoTrue(String tokenHash);

    @Modifying
    @Query("""
        update Sesion s
           set s.activo = false,
               s.fechaRevocacion = :fechaRevocacion,
               s.motivoRevocacion = :motivo
         where s.usuario.id = :usuarioId
           and s.activo = true
        """)
    int desactivarSesionesActivasPorUsuarioId(
            @Param("usuarioId") Integer usuarioId,
            @Param("fechaRevocacion") LocalDateTime fechaRevocacion,
            @Param("motivo") String motivo);
}
