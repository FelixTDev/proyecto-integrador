package pe.edu.utp.proyecto_integrador_casachantilly.fidelidad.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "puntos_fidelidad")
public class PuntosFidelidad {

    public enum TipoPuntos { GANADO, CANJEADO, AJUSTE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @Column(name = "pedido_id")
    private Integer pedidoId;

    @Column(nullable = false)
    private Integer puntos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TipoPuntos tipo;

    @Column(length = 200)
    private String descripcion;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "saldo_resultante")
    private Integer saldoResultante;
}
