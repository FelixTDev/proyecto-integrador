package pe.edu.utp.proyecto_integrador_casachantilly.pedido.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "pago")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "pedido_id", nullable = false)
    private Integer pedidoId;

    @Column(name = "metodo_pago_id", nullable = false)
    private Integer metodoPagoId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPago estado = EstadoPago.PENDIENTE;

    @Column(name = "referencia_externa", length = 200)
    private String referenciaExterna;

    @Column(nullable = false, length = 3, columnDefinition = "CHAR(3)")
    private String moneda = "PEN";

    @Column(name = "id_transaccion_externa", length = 120)
    private String idTransaccionExterna;

    @Column(name = "idempotency_key", length = 80)
    private String idempotencyKey;

    @Column(name = "codigo_error_proveedor", length = 80)
    private String codigoErrorProveedor;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @Column(nullable = false)
    private Integer intentos = 0;

    @Column(name = "proximo_intento")
    private LocalDateTime proximoIntento;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    public enum EstadoPago {
        PENDIENTE, APROBADO, RECHAZADO, REEMBOLSADO
    }
}
