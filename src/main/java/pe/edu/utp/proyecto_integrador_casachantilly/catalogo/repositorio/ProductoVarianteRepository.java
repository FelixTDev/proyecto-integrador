package pe.edu.utp.proyecto_integrador_casachantilly.catalogo.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.entidad.ProductoVariante;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoVarianteRepository extends JpaRepository<ProductoVariante, Integer> {
    List<ProductoVariante> findByProductoId(Integer productoId);
    List<ProductoVariante> findByProductoIdAndActivoTrue(Integer productoId);

    @Modifying
    @Query("update ProductoVariante v set v.activo = false where v.producto.id = :productoId and v.activo = true")
    int desactivarPorProductoId(@Param("productoId") Integer productoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from ProductoVariante v where v.id = :id")
    Optional<ProductoVariante> findByIdForUpdate(@Param("id") Integer id);
}
