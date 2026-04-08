package pe.edu.utp.proyecto_integrador_casachantilly.direccion.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "direccion")
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @Column(name = "zona_id")
    private Integer zonaId;

    @Column(length = 80)
    private String etiqueta;

    @Column(name = "direccion_completa", nullable = false, columnDefinition = "TEXT")
    private String direccionCompleta;

    @Column(columnDefinition = "TEXT")
    private String referencia;

    @Column(name = "destinatario_nombre", length = 150)
    private String destinatarioNombre;

    @Column(name = "destinatario_telefono", length = 25)
    private String destinatarioTelefono;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "es_principal", nullable = false)
    private Boolean esPrincipal = false;
}
