package pe.edu.utp.proyecto_integrador_casachantilly.catalogo.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.entidad.ProductoVariante;

import java.util.List;

@Repository
public interface ProductoVarianteRepository extends JpaRepository<ProductoVariante, Integer> {
    List<ProductoVariante> findByProductoId(Integer productoId);
    List<ProductoVariante> findByProductoIdAndActivoTrue(Integer productoId);
}
