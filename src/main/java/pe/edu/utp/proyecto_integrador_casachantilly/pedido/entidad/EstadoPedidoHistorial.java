package pe.edu.utp.proyecto_integrador_casachantilly.pedido.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "estado_pedido_historial")
public class EstadoPedidoHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "pedido_id", nullable = false)
    private Integer pedidoId;

    @Column(name = "estado_id", nullable = false)
    private Integer estadoId;

    @Column(name = "usuario_id")
    private Integer usuarioId;

    @Column(columnDefinition = "TEXT")
    private String observacion;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();
}
