package pe.edu.utp.proyecto_integrador_casachantilly.fidelidad.dto;

import java.util.List;

public record PuntosSaldoDTO(
    Integer saldo, List<PuntoMovimientoDTO> movimientos
) {}
