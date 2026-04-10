package pe.edu.utp.proyecto_integrador_casachantilly.personalizacion.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "personalizacion")
public class Personalizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "detalle_pedido_id", nullable = false, unique = true)
    private Integer detallePedidoId;

    @Column(name = "sabor_bizcocho", length = 100)
    private String saborBizcocho;

    @Column(name = "tipo_relleno", length = 100)
    private String tipoRelleno;

    @Column(name = "color_decorado", length = 100)
    private String colorDecorado;

    @Column(name = "texto_pastel", length = 200)
    private String textoPastel;

    @Column(name = "notas_cliente", columnDefinition = "TEXT")
    private String notasCliente;

    @Column(name = "imagen_referencia_url", columnDefinition = "TEXT")
    private String imagenReferenciaUrl;
}
