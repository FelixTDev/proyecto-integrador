package pe.edu.utp.proyecto_integrador_casachantilly.entrega.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "franja_horaria")
public class FranjaHoraria {

    public enum TipoFranja { DELIVERY, RECOJO, AMBOS }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(name = "cupos_totales", nullable = false)
    private Integer cuposTotales;

    @Column(name = "cupos_ocupados", nullable = false)
    private Integer cuposOcupados = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoFranja tipo = TipoFranja.AMBOS;
}
