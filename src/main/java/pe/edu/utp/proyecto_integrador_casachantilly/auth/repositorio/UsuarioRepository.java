package pe.edu.utp.proyecto_integrador_casachantilly.auth.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.entidad.Usuario;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    Optional<Usuario> findByResetToken(String resetToken);

    @Override
    default void deleteById(Integer id) {
        throw new UnsupportedOperationException("No se permite eliminación física de usuarios. Use desactivación lógica.");
    }

    @Override
    default void delete(Usuario entity) {
        throw new UnsupportedOperationException("No se permite eliminación física de usuarios. Use desactivación lógica.");
    }

    @Override
    default void deleteAllById(Iterable<? extends Integer> ids) {
        throw new UnsupportedOperationException("No se permite eliminación física de usuarios. Use desactivación lógica.");
    }

    @Override
    default void deleteAll(Iterable<? extends Usuario> entities) {
        throw new UnsupportedOperationException("No se permite eliminación física de usuarios. Use desactivación lógica.");
    }

    @Override
    default void deleteAll() {
        throw new UnsupportedOperationException("No se permite eliminación física de usuarios. Use desactivación lógica.");
    }
}
