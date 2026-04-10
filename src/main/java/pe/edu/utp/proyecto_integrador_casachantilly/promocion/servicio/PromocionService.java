package pe.edu.utp.proyecto_integrador_casachantilly.promocion.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.BadRequestException;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.ResourceNotFoundException;
import pe.edu.utp.proyecto_integrador_casachantilly.promocion.dto.CuponResultDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.promocion.dto.PromocionDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.promocion.dto.PromocionRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.promocion.entidad.Promocion;
import pe.edu.utp.proyecto_integrador_casachantilly.promocion.repositorio.PromocionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PromocionService {

    @Autowired private PromocionRepository promocionRepository;

    @Transactional(readOnly = true)
    public List<PromocionDTO> listarTodas() {
        return promocionRepository.findAllByOrderByActivoDescFechaFinAsc()
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<PromocionDTO> listarVigentes() {
        return promocionRepository.findVigentes(LocalDateTime.now())
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public PromocionDTO crear(PromocionRequest req) {
        Promocion p = new Promocion();
        mapRequest(req, p);
        promocionRepository.save(p);
        return toDto(p);
    }

    @Transactional
    public PromocionDTO editar(Integer id, PromocionRequest req) {
        Promocion p = promocionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada: " + id));
        mapRequest(req, p);
        promocionRepository.save(p);
        return toDto(p);
    }

    @Transactional
    public boolean toggleActivo(Integer id) {
        Promocion p = promocionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada: " + id));
        p.setActivo(!p.getActivo());
        promocionRepository.save(p);
        return p.getActivo();
    }

    @Transactional(readOnly = true)
    public CuponResultDTO validarCupon(String codigoCupon, BigDecimal subtotalCarrito) {
        var opt = promocionRepository.findByCodigoCuponIgnoreCase(codigoCupon.trim());
        if (opt.isEmpty()) {
            return new CuponResultDTO(false, "Cupón no encontrado", BigDecimal.ZERO, null, null);
        }
        Promocion p = opt.get();
        if (!p.getActivo()) {
            return new CuponResultDTO(false, "Cupón inactivo", BigDecimal.ZERO, null, null);
        }
        LocalDateTime ahora = LocalDateTime.now();
        if (p.getFechaInicio() != null && p.getFechaInicio().isAfter(ahora)) {
            return new CuponResultDTO(false, "Cupón aún no vigente", BigDecimal.ZERO, null, null);
        }
        if (p.getFechaFin() != null && p.getFechaFin().isBefore(ahora)) {
            return new CuponResultDTO(false, "Cupón expirado", BigDecimal.ZERO, null, null);
        }
        if (p.getMontoMinimo() != null && subtotalCarrito.compareTo(p.getMontoMinimo()) < 0) {
            return new CuponResultDTO(false, "Monto mínimo no alcanzado (S/." + p.getMontoMinimo() + ")",
                    BigDecimal.ZERO, null, null);
        }

        BigDecimal descuento = calcularDescuento(p, subtotalCarrito);
        return new CuponResultDTO(true, "Cupón aplicado: " + p.getNombre(), descuento,
                p.getTipoDescuento(), p.getNombre());
    }

    public BigDecimal calcularDescuento(Promocion p, BigDecimal subtotal) {
        String tipo = p.getTipoDescuento();
        BigDecimal valor = p.getValorDescuento();
        return switch (tipo) {
            case "PORCENTAJE" -> subtotal.multiply(valor).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case "MONTO_FIJO" -> valor.min(subtotal);
            case "2X1" -> subtotal.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            case "ENVIO_GRATIS" -> BigDecimal.ZERO;
            default -> BigDecimal.ZERO;
        };
    }

    public boolean isEnvioGratis(Promocion p) {
        return "ENVIO_GRATIS".equals(p.getTipoDescuento());
    }

    private void mapRequest(PromocionRequest req, Promocion p) {
        p.setNombre(req.nombre().trim());
        p.setTipoDescuento(req.tipoDescuento().trim());
        p.setValorDescuento(req.valorDescuento());
        p.setMontoMinimo(req.montoMinimo() != null ? req.montoMinimo() : BigDecimal.ZERO);
        p.setAplicaA(req.aplicaA() != null ? req.aplicaA() : "PRODUCTO");
        p.setFechaInicio(req.fechaInicio());
        p.setFechaFin(req.fechaFin());
        p.setActivo(req.activo() != null ? req.activo() : true);
        p.setCodigoCupon(req.codigoCupon() != null ? req.codigoCupon().trim().toUpperCase() : null);
        p.setLimiteUsosTotal(req.limiteUsosTotal());
        p.setLimiteUsosPorUsuario(req.limiteUsosPorUsuario());
    }

    private PromocionDTO toDto(Promocion p) {
        String estado;
        if (!p.getActivo()) estado = "INACTIVA";
        else if (p.getFechaFin() != null && p.getFechaFin().isBefore(LocalDateTime.now())) estado = "EXPIRADA";
        else if (p.getFechaInicio() != null && p.getFechaInicio().isAfter(LocalDateTime.now())) estado = "PROGRAMADA";
        else estado = "ACTIVA";

        return new PromocionDTO(p.getId(), p.getNombre(), p.getTipoDescuento(), p.getValorDescuento(),
                p.getMontoMinimo(), p.getAplicaA(), p.getFechaInicio(), p.getFechaFin(),
                p.getActivo(), p.getCodigoCupon(), p.getLimiteUsosTotal(),
                p.getLimiteUsosPorUsuario(), estado);
    }
}
