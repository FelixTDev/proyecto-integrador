package pe.edu.utp.proyecto_integrador_casachantilly.carrito.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.utp.proyecto_integrador_casachantilly.carrito.entidad.CarritoDetalle;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarritoDetalleRepository extends JpaRepository<CarritoDetalle, Integer> {
    List<CarritoDetalle> findByCarritoId(Integer carritoId);
    Optional<CarritoDetalle> findByCarritoIdAndVarianteId(Integer carritoId, Integer varianteId);
    void deleteByCarritoId(Integer carritoId);
}
