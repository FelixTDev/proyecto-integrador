package pe.edu.utp.proyecto_integrador_casachantilly.entrega.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.BadRequestException;
import pe.edu.utp.proyecto_integrador_casachantilly.entrega.dto.FranjaHorariaDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.entrega.entidad.FranjaHoraria;
import pe.edu.utp.proyecto_integrador_casachantilly.entrega.repositorio.FranjaHorariaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class FranjaHorariaService {

    @Autowired private FranjaHorariaRepository franjaHorariaRepository;

    @Transactional(readOnly = true)
    public List<FranjaHorariaDTO> listarDisponiblesPorFecha(LocalDate fecha) {
        if (fecha.isBefore(LocalDate.now())) {
            throw new BadRequestException("No se pueden consultar franjas pasadas");
        }
        return franjaHorariaRepository.findByFechaOrderByHoraInicio(fecha)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<FranjaHorariaDTO> listarFuturas() {
        return franjaHorariaRepository.findFuturas(LocalDate.now())
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public FranjaHorariaDTO crearFranja(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                         Integer cuposTotales, String tipo) {
        if (fecha.isBefore(LocalDate.now())) {
            throw new BadRequestException("No se puede crear franja en fecha pasada");
        }
        FranjaHoraria f = new FranjaHoraria();
        f.setFecha(fecha);
        f.setHoraInicio(horaInicio);
        f.setHoraFin(horaFin);
        f.setCuposTotales(cuposTotales);
        f.setTipo(FranjaHoraria.TipoFranja.valueOf(tipo));
        franjaHorariaRepository.save(f);
        return toDto(f);
    }

    private FranjaHorariaDTO toDto(FranjaHoraria f) {
        int disponibles = f.getCuposTotales() - f.getCuposOcupados();
        return new FranjaHorariaDTO(f.getId(), f.getFecha(), f.getHoraInicio(), f.getHoraFin(),
                f.getCuposTotales(), f.getCuposOcupados(), disponibles,
                f.getTipo().name(), disponibles > 0);
    }
}
