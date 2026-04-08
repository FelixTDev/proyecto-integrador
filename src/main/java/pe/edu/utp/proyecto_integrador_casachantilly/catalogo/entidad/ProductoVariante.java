package pe.edu.utp.proyecto_integrador_casachantilly.catalogo.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "producto_variante")
public class ProductoVariante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "nombre_variante", nullable = false, length = 100)
    private String nombreVariante;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(precision = 10, scale = 2)
    private BigDecimal costo;

    @Column(name = "peso_gramos")
    private Integer pesoGramos;

    @Column(name = "tiempo_prep_min", nullable = false)
    private Integer tiempoPrepMin = 60;

    @Column(name = "stock_disponible", nullable = false)
    private Integer stockDisponible = 0;

    @Column(name = "codigo_sku", length = 60)
    private String codigoSku;

    @Column(name = "codigo_barras", length = 32)
    private String codigoBarras;

    @Column(nullable = false)
    private Boolean activo = true;
}
