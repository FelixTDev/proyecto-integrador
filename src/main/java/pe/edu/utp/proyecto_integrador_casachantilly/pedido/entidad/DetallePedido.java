package pe.edu.utp.proyecto_integrador_casachantilly.pedido.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "detalle_pedido")
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Column(name = "variante_id", nullable = false)
    private Integer varianteId;

    @Column(name = "nombre_snapshot", nullable = false, length = 200)
    private String nombreSnapshot;

    @Column(name = "precio_unitario_snapshot", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitarioSnapshot;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "subtotal_linea", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotalLinea;
}
