package pe.edu.utp.proyecto_integrador_casachantilly.entrega.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record FranjaHorariaDTO(
    Integer id, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
    Integer cuposTotales, Integer cuposOcupados, Integer cuposDisponibles,
    String tipo, boolean disponible
) {}
