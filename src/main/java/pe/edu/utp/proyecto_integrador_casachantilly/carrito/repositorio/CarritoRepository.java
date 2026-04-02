package pe.edu.utp.proyecto_integrador_casachantilly.carrito.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.utp.proyecto_integrador_casachantilly.carrito.entidad.Carrito;

import java.util.Optional;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Integer> {
    Optional<Carrito> findFirstByUsuarioIdOrderByFechaCreacionDesc(Integer usuarioId);
}
