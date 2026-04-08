package pe.edu.utp.proyecto_integrador_casachantilly.direccion.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.entidad.Usuario;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.repositorio.UsuarioRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.BadRequestException;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.ResourceNotFoundException;
import pe.edu.utp.proyecto_integrador_casachantilly.direccion.dto.DireccionDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.direccion.dto.DireccionRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.direccion.dto.ZonaEnvioDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.direccion.entidad.Direccion;
import pe.edu.utp.proyecto_integrador_casachantilly.direccion.entidad.ZonaEnvio;
import pe.edu.utp.proyecto_integrador_casachantilly.direccion.repositorio.DireccionRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.direccion.repositorio.ZonaEnvioRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DireccionService {

    @Autowired private DireccionRepository direccionRepository;
    @Autowired private ZonaEnvioRepository zonaEnvioRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<DireccionDTO> listarDirecciones(Integer usuarioId, boolean incluirInactivas) {
        List<Direccion> direcciones = incluirInactivas
                ? direccionRepository.findByUsuarioIdOrderByEsPrincipalDescIdDesc(usuarioId)
                : direccionRepository.findByUsuarioIdAndActivoTrueOrderByEsPrincipalDescIdDesc(usuarioId);

        return mapDirecciones(direcciones);
    }

    @Transactional
    public DireccionDTO crearDireccion(Integer usuarioId, DireccionRequest req) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        validarZonaSiExiste(req.zonaId());

        Direccion direccion = new Direccion();
        direccion.setUsuarioId(usuarioId);
        direccion.setZonaId(req.zonaId());
        direccion.setEtiqueta(normalize(req.etiqueta()));
        direccion.setDireccionCompleta(req.direccionCompleta().trim());
        direccion.setReferencia(normalize(req.referencia()));
        direccion.setDestinatarioNombre(resolveDestinatarioNombre(req.destinatarioNombre(), usuario));
        direccion.setDestinatarioTelefono(resolveDestinatarioTelefono(req.destinatarioTelefono(), usuario));
        direccion.setActivo(true);
        direccion.setEsPrincipal(false);
        direccionRepository.save(direccion);

        boolean noHayPrincipal = direccionRepository.findFirstByUsuarioIdAndEsPrincipalTrueAndActivoTrue(usuarioId).isEmpty();
        if (Boolean.TRUE.equals(req.esPrincipal()) || noHayPrincipal) {
            setPrincipalInterno(usuarioId, direccion.getId());
        }

        return toDTO(direccionRepository.findById(direccion.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada")));
    }

    @Transactional
    public DireccionDTO actualizarDireccion(Integer usuarioId, Integer direccionId, DireccionRequest req) {
        Direccion direccion = direccionRepository.findByIdAndUsuarioId(direccionId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada"));

        validarZonaSiExiste(req.zonaId());

        direccion.setZonaId(req.zonaId());
        direccion.setEtiqueta(normalize(req.etiqueta()));
        direccion.setDireccionCompleta(req.direccionCompleta().trim());
        direccion.setReferencia(normalize(req.referencia()));
        if (req.destinatarioNombre() != null && !req.destinatarioNombre().isBlank()) {
            direccion.setDestinatarioNombre(req.destinatarioNombre().trim());
        }
        if (req.destinatarioTelefono() != null && !req.destinatarioTelefono().isBlank()) {
            direccion.setDestinatarioTelefono(req.destinatarioTelefono().trim());
        }

        direccionRepository.save(direccion);

        if (Boolean.TRUE.equals(req.esPrincipal()) && Boolean.TRUE.equals(direccion.getActivo())) {
            setPrincipalInterno(usuarioId, direccion.getId());
        }

        return toDTO(direccion);
    }

    @Transactional
    public void desactivarDireccion(Integer usuarioId, Integer direccionId) {
        Direccion direccion = direccionRepository.findByIdAndUsuarioId(direccionId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada"));

        if (!Boolean.TRUE.equals(direccion.getActivo())) {
            return;
        }

        boolean eraPrincipal = Boolean.TRUE.equals(direccion.getEsPrincipal());
        direccion.setActivo(false);
        direccion.setEsPrincipal(false);
        direccionRepository.save(direccion);

        if (eraPrincipal) {
            List<Direccion> activas = direccionRepository.findByUsuarioIdAndActivoTrueOrderByEsPrincipalDescIdDesc(usuarioId);
            if (!activas.isEmpty()) {
                setPrincipalInterno(usuarioId, activas.get(0).getId());
            }
        }
    }

    @Transactional
    public DireccionDTO marcarPrincipal(Integer usuarioId, Integer direccionId) {
        Direccion direccion = direccionRepository.findByIdAndUsuarioId(direccionId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada"));
        if (!Boolean.TRUE.equals(direccion.getActivo())) {
            throw new BadRequestException("No se puede marcar como principal una dirección desactivada");
        }

        setPrincipalInterno(usuarioId, direccionId);
        Direccion principal = direccionRepository.findByIdAndUsuarioId(direccionId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada"));
        return toDTO(principal);
    }

    @Transactional(readOnly = true)
    public Direccion obtenerDireccionActivaUsuario(Integer usuarioId, Integer direccionId) {
        Direccion direccion = direccionRepository.findByIdAndUsuarioId(direccionId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada"));
        if (!Boolean.TRUE.equals(direccion.getActivo())) {
            throw new BadRequestException("La dirección seleccionada está desactivada");
        }
        return direccion;
    }

    @Transactional(readOnly = true)
    public List<ZonaEnvioDTO> listarZonasActivas() {
        return zonaEnvioRepository.findByActivoTrueOrderByNombreDistritoAsc().stream()
                .map(z -> new ZonaEnvioDTO(z.getId(), z.getNombreDistrito(), z.getCostoDelivery(), z.getTiempoEstimadoMin()))
                .toList();
    }

    private List<DireccionDTO> mapDirecciones(List<Direccion> direcciones) {
        List<Integer> zonaIds = direcciones.stream()
                .map(Direccion::getZonaId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        Map<Integer, String> nombresZona = zonaEnvioRepository.findAllById(zonaIds).stream()
                .collect(Collectors.toMap(ZonaEnvio::getId, ZonaEnvio::getNombreDistrito));

        return direcciones.stream()
                .map(d -> new DireccionDTO(
                        d.getId(),
                        d.getUsuarioId(),
                        d.getZonaId(),
                        nombresZona.get(d.getZonaId()),
                        d.getEtiqueta(),
                        d.getDireccionCompleta(),
                        d.getReferencia(),
                        d.getDestinatarioNombre(),
                        d.getDestinatarioTelefono(),
                        d.getActivo(),
                        d.getEsPrincipal()
                ))
                .toList();
    }

    private DireccionDTO toDTO(Direccion d) {
        String zonaNombre = null;
        if (d.getZonaId() != null) {
            zonaNombre = zonaEnvioRepository.findById(d.getZonaId()).map(ZonaEnvio::getNombreDistrito).orElse(null);
        }
        return new DireccionDTO(
                d.getId(),
                d.getUsuarioId(),
                d.getZonaId(),
                zonaNombre,
                d.getEtiqueta(),
                d.getDireccionCompleta(),
                d.getReferencia(),
                d.getDestinatarioNombre(),
                d.getDestinatarioTelefono(),
                d.getActivo(),
                d.getEsPrincipal()
        );
    }

    private void setPrincipalInterno(Integer usuarioId, Integer direccionPrincipalId) {
        List<Direccion> direcciones = direccionRepository.findByUsuarioIdOrderByEsPrincipalDescIdDesc(usuarioId);
        for (Direccion d : direcciones) {
            boolean principal = d.getId().equals(direccionPrincipalId);
            if (d.getEsPrincipal() != principal) {
                d.setEsPrincipal(principal);
                direccionRepository.save(d);
            }
        }
    }

    private String resolveDestinatarioNombre(String nombre, Usuario usuario) {
        if (nombre != null && !nombre.isBlank()) return nombre.trim();
        return usuario.getNombre();
    }

    private String resolveDestinatarioTelefono(String telefono, Usuario usuario) {
        if (telefono != null && !telefono.isBlank()) return telefono.trim();
        return usuario.getTelefono();
    }

    private String normalize(String valor) {
        if (valor == null) return null;
        String t = valor.trim();
        return t.isEmpty() ? null : t;
    }

    private void validarZonaSiExiste(Integer zonaId) {
        if (zonaId == null) return;
        zonaEnvioRepository.findByIdAndActivoTrue(zonaId)
                .orElseThrow(() -> new BadRequestException("La zona de envío seleccionada no está disponible"));
    }
}
