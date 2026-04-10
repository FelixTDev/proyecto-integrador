package pe.edu.utp.proyecto_integrador_casachantilly.promocion.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "promocion")
public class Promocion {

    public enum TipoDescuento {
        PORCENTAJE, MONTO_FIJO, @SuppressWarnings("java:S115") TWO_X_ONE, ENVIO_GRATIS;

        private static final java.util.Map<String, TipoDescuento> DB_MAP = java.util.Map.of(
                "PORCENTAJE", PORCENTAJE, "MONTO_FIJO", MONTO_FIJO,
                "2X1", TWO_X_ONE, "ENVIO_GRATIS", ENVIO_GRATIS
        );

        public static TipoDescuento fromDbValue(String v) {
            TipoDescuento td = DB_MAP.get(v);
            if (td == null) throw new IllegalArgumentException("Tipo descuento desconocido: " + v);
            return td;
        }
    }

    public enum AplicaA { PRODUCTO, CARRITO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(name = "tipo_descuento", nullable = false, length = 30)
    private String tipoDescuento;

    @Column(name = "valor_descuento", nullable = false, precision = 8, scale = 2)
    private BigDecimal valorDescuento = BigDecimal.ZERO;

    @Column(name = "monto_minimo", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoMinimo = BigDecimal.ZERO;

    @Column(name = "aplica_a", nullable = false, length = 20)
    private String aplicaA = "PRODUCTO";

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "codigo_cupon", length = 40, unique = true)
    private String codigoCupon;

    @Column(name = "limite_usos_total")
    private Integer limiteUsosTotal;

    @Column(name = "limite_usos_por_usuario")
    private Integer limiteUsosPorUsuario;
}
