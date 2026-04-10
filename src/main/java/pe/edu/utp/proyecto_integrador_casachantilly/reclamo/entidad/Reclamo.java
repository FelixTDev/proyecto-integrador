package pe.edu.utp.proyecto_integrador_casachantilly.reclamo.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "reclamo")
public class Reclamo {

    public enum TipoReclamo { REEMBOLSO, REPOSICION, QUEJA }
    public enum EstadoReclamo { ABIERTO, EN_REVISION, RESUELTO, CERRADO }
    public enum Prioridad { BAJA, MEDIA, ALTA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "pedido_id", nullable = false)
    private Integer pedidoId;

    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoReclamo tipo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReclamo estado = EstadoReclamo.ABIERTO;

    @Column(name = "monto_reembolso", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoReembolso = BigDecimal.ZERO;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Prioridad prioridad = Prioridad.MEDIA;

    @Column(name = "detalle_resolucion", columnDefinition = "TEXT")
    private String detalleResolucion;
}
