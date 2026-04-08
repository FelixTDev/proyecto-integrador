package pe.edu.utp.proyecto_integrador_casachantilly.direccion.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "zona_envio")
public class ZonaEnvio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre_distrito", nullable = false, length = 120)
    private String nombreDistrito;

    @Column(name = "costo_delivery", nullable = false, precision = 8, scale = 2)
    private BigDecimal costoDelivery = BigDecimal.ZERO;

    @Column(name = "tiempo_estimado_min", nullable = false)
    private Integer tiempoEstimadoMin = 60;

    @Column(nullable = false)
    private Boolean activo = true;
}
