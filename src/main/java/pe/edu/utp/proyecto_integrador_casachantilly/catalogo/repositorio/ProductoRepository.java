package pe.edu.utp.proyecto_integrador_casachantilly.catalogo.repositorio;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.entidad.Producto;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.entidad.ProductoVariante;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    /**
     * Consulta personalizada requerida (RF01):
     * Devuelve variantes activas con stock de una categoría específica.
     */
    @Query("SELECT pv FROM ProductoVariante pv " +
           "WHERE pv.producto.categoria.id = :catId " +
           "AND pv.activo = true " +
           "AND pv.stockDisponible > 0")
    Page<ProductoVariante> findVariantesActivasByCategoria(@Param("catId") Integer catId, Pageable pageable);

    /** Todos los productos activos (paginado). */
    Page<Producto> findByActivoTrue(Pageable pageable);

    /** Productos activos por categoría (paginado). */
    Page<Producto> findByCategoriaIdAndActivoTrue(Integer categoriaId, Pageable pageable);
}
