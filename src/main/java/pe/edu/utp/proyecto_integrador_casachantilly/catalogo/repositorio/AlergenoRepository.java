package pe.edu.utp.proyecto_integrador_casachantilly.catalogo.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.entidad.Alergeno;

@Repository
public interface AlergenoRepository extends JpaRepository<Alergeno, Integer> {
}
