package pe.edu.utp.proyecto_integrador_casachantilly.carrito.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.edu.utp.proyecto_integrador_casachantilly.catalogo.entidad.ProductoVariante;

@Getter
@Setter
@Entity
@Table(name = "carrito_detalle")
public class CarritoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrito_id", nullable = false)
    private Carrito carrito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variante_id", nullable = false)
    private ProductoVariante variante;

    @Column(nullable = false)
    private Integer cantidad = 1;
}
