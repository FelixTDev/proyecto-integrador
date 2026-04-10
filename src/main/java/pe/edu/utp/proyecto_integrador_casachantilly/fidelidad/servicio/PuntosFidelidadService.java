package pe.edu.utp.proyecto_integrador_casachantilly.fidelidad.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.BadRequestException;
import pe.edu.utp.proyecto_integrador_casachantilly.fidelidad.dto.PuntoMovimientoDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.fidelidad.dto.PuntosSaldoDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.fidelidad.entidad.PuntosFidelidad;
import pe.edu.utp.proyecto_integrador_casachantilly.fidelidad.repositorio.PuntosFidelidadRepository;

import java.util.List;

@Service
public class PuntosFidelidadService {

    @Autowired private PuntosFidelidadRepository puntosRepo;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public PuntosSaldoDTO obtenerSaldoYMovimientos(Integer usuarioId) {
        int saldo = puntosRepo.obtenerSaldo(usuarioId);
        List<PuntoMovimientoDTO> movimientos = puntosRepo.findByUsuarioIdOrderByFechaDesc(usuarioId)
                .stream().map(this::toDto).toList();
        return new PuntosSaldoDTO(saldo, movimientos);
    }

    @Transactional
    public void asignarPuntosPorPedido(Integer pedidoId) {
        try {
            jdbcTemplate.update("CALL sp_asignar_puntos(?)", pedidoId);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("ya fueron asignados")) {
                return;
            }
            throw e;
        }
    }

    @Transactional
    public PuntosSaldoDTO canjearPuntos(Integer usuarioId, Integer puntos, Integer pedidoId) {
        if (puntos <= 0) {
            throw new BadRequestException("Los puntos a canjear deben ser mayor a 0");
        }
        int saldo = puntosRepo.obtenerSaldo(usuarioId);
        if (saldo < puntos) {
            throw new BadRequestException("Saldo insuficiente. Puntos disponibles: " + saldo);
        }
        jdbcTemplate.update("CALL sp_canjear_puntos(?, ?, ?)", usuarioId, puntos, pedidoId);
        return obtenerSaldoYMovimientos(usuarioId);
    }

    private PuntoMovimientoDTO toDto(PuntosFidelidad p) {
        return new PuntoMovimientoDTO(p.getId(), p.getPedidoId(), p.getPuntos(),
                p.getTipo().name(), p.getDescripcion(), p.getFecha());
    }
}
