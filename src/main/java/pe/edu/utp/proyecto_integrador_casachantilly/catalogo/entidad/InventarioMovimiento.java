package pe.edu.utp.proyecto_integrador_casachantilly.catalogo.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "inventario_movimiento")
public class InventarioMovimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variante_id", nullable = false)
    private ProductoVariante variante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimiento tipo;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "stock_resultante", nullable = false)
    private Integer stockResultante;

    @Column(length = 150)
    private String motivo;

    @Column(name = "pedido_id")
    private Integer pedidoId;

    @Column(name = "usuario_id")
    private Integer usuarioId;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();
}
