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
import java.util.Locale;

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
                .orElseThrow(() -> new ResourceNotFoundException("Promocion no encontrada: " + id));
        mapRequest(req, p);
        promocionRepository.save(p);
        return toDto(p);
    }

    @Transactional
    public boolean toggleActivo(Integer id) {
        Promocion p = promocionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promocion no encontrada: " + id));
        p.setActivo(!p.getActivo());
        promocionRepository.save(p);
        return p.getActivo();
    }

    @Transactional(readOnly = true)
    public CuponResultDTO validarCupon(String codigoCupon, BigDecimal subtotalCarrito) {
        return validarCupon(codigoCupon, subtotalCarrito, BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public CuponResultDTO validarCupon(String codigoCupon, BigDecimal subtotalCarrito, BigDecimal costoEnvio) {
        if (codigoCupon == null || codigoCupon.isBlank()) {
            return resultadoNoAplicado("Cupon vacio", subtotalCarrito, costoEnvio);
        }
        if (subtotalCarrito == null || subtotalCarrito.compareTo(BigDecimal.ZERO) <= 0) {
            return resultadoNoAplicado("Subtotal invalido", subtotalCarrito, costoEnvio);
        }

        var opt = promocionRepository.findByCodigoCuponIgnoreCase(codigoCupon.trim());
        if (opt.isEmpty()) {
            return resultadoNoAplicado("Cupon no encontrado", subtotalCarrito, costoEnvio);
        }

        Promocion p = opt.get();
        String tipo = normalizarTipoDescuento(p.getTipoDescuento());
        if (!p.getActivo()) {
            return resultadoNoAplicado("Cupon inactivo", subtotalCarrito, costoEnvio, tipo, p.getNombre());
        }

        LocalDateTime ahora = LocalDateTime.now();
        if (p.getFechaInicio() != null && p.getFechaInicio().isAfter(ahora)) {
            return resultadoNoAplicado("Cupon aun no vigente", subtotalCarrito, costoEnvio, tipo, p.getNombre());
        }
        if (p.getFechaFin() != null && p.getFechaFin().isBefore(ahora)) {
            return resultadoNoAplicado("Cupon expirado", subtotalCarrito, costoEnvio, tipo, p.getNombre());
        }
        if (p.getMontoMinimo() != null && subtotalCarrito.compareTo(p.getMontoMinimo()) < 0) {
            return resultadoNoAplicado("Monto minimo no alcanzado (S/." + p.getMontoMinimo() + ")",
                    subtotalCarrito, costoEnvio, tipo, p.getNombre());
        }

        BigDecimal descuentoProductos = calcularDescuento(p, subtotalCarrito);
        boolean envioGratisAplicado = isEnvioGratis(p);
        BigDecimal costoEnvioOriginal = costoEnvio == null ? BigDecimal.ZERO : costoEnvio.max(BigDecimal.ZERO);
        BigDecimal costoEnvioFinal = envioGratisAplicado ? BigDecimal.ZERO : costoEnvioOriginal;
        BigDecimal descuentoTotal = descuentoProductos.add(costoEnvioOriginal.subtract(costoEnvioFinal));

        return new CuponResultDTO(
                true,
                "Cupon aplicado: " + p.getNombre(),
                descuentoTotal,
                tipo,
                p.getNombre(),
                descuentoProductos,
                envioGratisAplicado,
                costoEnvioOriginal,
                costoEnvioFinal
        );
    }

    public BigDecimal calcularDescuento(Promocion p, BigDecimal subtotal) {
        String tipo = normalizarTipoDescuento(p.getTipoDescuento());
        BigDecimal valor = p.getValorDescuento() == null ? BigDecimal.ZERO : p.getValorDescuento();
        return switch (tipo) {
            case "PORCENTAJE" -> subtotal.multiply(valor)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    .min(subtotal);
            case "MONTO_FIJO" -> valor.min(subtotal);
            case "2X1" -> subtotal.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            case "ENVIO_GRATIS" -> BigDecimal.ZERO;
            default -> BigDecimal.ZERO;
        };
    }

    public boolean isEnvioGratis(Promocion p) {
        return "ENVIO_GRATIS".equals(normalizarTipoDescuento(p.getTipoDescuento()));
    }

    private void mapRequest(PromocionRequest req, Promocion p) {
        if (req.nombre() == null || req.nombre().isBlank()) {
            throw new BadRequestException("El nombre de promocion es requerido");
        }
        String tipoDescuento = normalizarTipoDescuento(req.tipoDescuento());
        String aplicaA = normalizarAplicaA(req.aplicaA());

        p.setNombre(req.nombre().trim());
        p.setTipoDescuento(tipoDescuento);
        p.setValorDescuento(req.valorDescuento() == null ? BigDecimal.ZERO : req.valorDescuento());
        p.setMontoMinimo(req.montoMinimo() != null ? req.montoMinimo() : BigDecimal.ZERO);
        p.setAplicaA(aplicaA);
        p.setFechaInicio(req.fechaInicio());
        p.setFechaFin(req.fechaFin());
        p.setActivo(req.activo() != null ? req.activo() : true);
        p.setCodigoCupon(req.codigoCupon() != null ? req.codigoCupon().trim().toUpperCase(Locale.ROOT) : null);
        p.setLimiteUsosTotal(req.limiteUsosTotal());
        p.setLimiteUsosPorUsuario(req.limiteUsosPorUsuario());

        if ("ENVIO_GRATIS".equals(tipoDescuento)) {
            p.setValorDescuento(BigDecimal.ZERO);
        }
        if ("PORCENTAJE".equals(tipoDescuento)
                && (p.getValorDescuento().compareTo(BigDecimal.ZERO) <= 0 || p.getValorDescuento().compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new BadRequestException("PORCENTAJE debe estar entre 0 y 100");
        }
        if (("MONTO_FIJO".equals(tipoDescuento) || "2X1".equals(tipoDescuento))
                && p.getValorDescuento().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("El valor del descuento no puede ser negativo");
        }
    }

    private PromocionDTO toDto(Promocion p) {
        LocalDateTime ahora = LocalDateTime.now();
        String estado;
        if (!p.getActivo()) estado = "INACTIVA";
        else if (p.getFechaFin() != null && p.getFechaFin().isBefore(ahora)) estado = "EXPIRADA";
        else if (p.getFechaInicio() != null && p.getFechaInicio().isAfter(ahora)) estado = "PROGRAMADA";
        else estado = "ACTIVA";

        return new PromocionDTO(
                p.getId(),
                p.getNombre(),
                normalizarTipoDescuento(p.getTipoDescuento()),
                p.getValorDescuento(),
                p.getMontoMinimo(),
                normalizarAplicaA(p.getAplicaA()),
                p.getFechaInicio(),
                p.getFechaFin(),
                p.getActivo(),
                p.getCodigoCupon(),
                p.getLimiteUsosTotal(),
                p.getLimiteUsosPorUsuario(),
                estado
        );
    }

    private CuponResultDTO resultadoNoAplicado(String mensaje, BigDecimal subtotal, BigDecimal costoEnvio) {
        return resultadoNoAplicado(mensaje, subtotal, costoEnvio, null, null);
    }

    private CuponResultDTO resultadoNoAplicado(String mensaje, BigDecimal subtotal, BigDecimal costoEnvio, String tipo, String nombre) {
        BigDecimal envio = costoEnvio == null ? BigDecimal.ZERO : costoEnvio.max(BigDecimal.ZERO);
        return new CuponResultDTO(false, mensaje, BigDecimal.ZERO, tipo, nombre,
                BigDecimal.ZERO, false, envio, envio);
    }

    private String normalizarTipoDescuento(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BadRequestException("tipoDescuento es requerido");
        }
        String normalizado = raw.trim().toUpperCase(Locale.ROOT).replace("_", "");
        return switch (normalizado) {
            case "PORCENTAJE" -> "PORCENTAJE";
            case "MONTOFIJO" -> "MONTO_FIJO";
            case "2X1", "TWOXONE" -> "2X1";
            case "ENVIOGRATIS" -> "ENVIO_GRATIS";
            default -> throw new BadRequestException("tipoDescuento invalido. Valores: PORCENTAJE, MONTO_FIJO, 2X1, ENVIO_GRATIS");
        };
    }

    private String normalizarAplicaA(String raw) {
        if (raw == null || raw.isBlank()) {
            return "CARRITO";
        }
        String normalizado = raw.trim().toUpperCase(Locale.ROOT);
        if (!"PRODUCTO".equals(normalizado) && !"CARRITO".equals(normalizado)) {
            throw new BadRequestException("aplicaA invalido. Valores: PRODUCTO o CARRITO");
        }
        return normalizado;
    }
}