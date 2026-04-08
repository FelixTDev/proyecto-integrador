package pe.edu.utp.proyecto_integrador_casachantilly.auth.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.dto.AuthResponse;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.dto.AdminCrearEmpleadoRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.dto.LoginRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.dto.RegistroRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.entidad.Rol;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.entidad.Sesion;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.entidad.Usuario;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.repositorio.RolRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.repositorio.SesionRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.repositorio.UsuarioRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.BadRequestException;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.ResourceNotFoundException;
import pe.edu.utp.proyecto_integrador_casachantilly.config.JwtUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class AuthService {

    public enum SesionEstado {
        ACTIVA, EXPIRADA, REVOCADA, NO_ENCONTRADA
    }

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private SesionRepository sesionRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UsuarioDetailsService usuarioDetailsService;
    @org.springframework.beans.factory.annotation.Value("${app.session.inactivity-minutes:30}")
    private long inactividadMinutos;

    // ─── Registro ──────────────────────────────────────────────

    @Transactional
    public AuthResponse registro(RegistroRequest req) {
        String nombre = req.nombre() == null ? "" : req.nombre().trim().replaceAll("\\s+", " ");
        String email = req.email() == null ? "" : req.email().trim().toLowerCase();
        String password = req.password() == null ? "" : req.password().trim();
        String telefono = req.telefono() == null ? null : req.telefono().trim();

        if (nombre.isBlank()) {
            throw new BadRequestException("El nombre es requerido");
        }
        if (!nombre.matches("^[\\p{L}\\s]+$")) {
            throw new BadRequestException("El nombre solo debe contener letras y espacios");
        }
        if (password.length() < 6) {
            throw new BadRequestException("La contraseña debe tener al menos 6 caracteres");
        }
        if (telefono != null && !telefono.isBlank() && !telefono.matches("^\\d{9}$")) {
            throw new BadRequestException("El número de celular debe tener 9 dígitos");
        }

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("Ya existe un usuario con el email: " + email);
        }

        Rol rolCliente = rolRepository.findByNombre("CLIENTE")
                .orElseThrow(() -> new BadRequestException("Rol CLIENTE no encontrado en BD"));

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(password));
        usuario.setTelefono(telefono == null || telefono.isBlank() ? null : telefono);
        usuario.setFechaActualizacion(LocalDateTime.now());
        usuario.setIntentosFallidosLogin(0);
        usuario.getRoles().add(rolCliente);
        usuarioRepository.save(usuario);

        // Generar token
        UserDetails ud = usuarioDetailsService.loadUserByUsername(email);
        String token = jwtUtil.generarToken(ud.getUsername(), ud.getAuthorities());

        // Guardar sesión
        guardarSesion(usuario, token, null, null);

        return AuthResponse.of(token, email);
    }

    @Transactional
    public Integer crearEmpleado(AdminCrearEmpleadoRequest req) {
        String nombre = req.nombre() == null ? "" : req.nombre().trim().replaceAll("\\s+", " ");
        String email = req.email() == null ? "" : req.email().trim().toLowerCase();
        String password = req.password() == null ? "" : req.password().trim();
        String telefono = req.telefono() == null ? null : req.telefono().trim();
        String rolNombre = req.rol() == null ? "" : req.rol().trim().toUpperCase();

        if (nombre.isBlank()) {
            throw new BadRequestException("El nombre es requerido");
        }
        if (!nombre.matches("^[\\p{L}\\s]+$")) {
            throw new BadRequestException("El nombre solo debe contener letras y espacios");
        }
        if (password.length() < 6) {
            throw new BadRequestException("La contraseña debe tener al menos 6 caracteres");
        }
        if (telefono != null && !telefono.isBlank() && !telefono.matches("^\\d{9}$")) {
            throw new BadRequestException("El número de celular debe tener 9 dígitos");
        }
        if (!("ADMIN".equals(rolNombre) || "VENDEDOR".equals(rolNombre))) {
            throw new BadRequestException("Rol inválido. Use ADMIN o VENDEDOR");
        }
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("Ya existe un usuario con el email: " + email);
        }

        Rol rol = rolRepository.findByNombre(rolNombre)
                .orElseThrow(() -> new BadRequestException("Rol no encontrado en BD: " + rolNombre));

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(password));
        usuario.setTelefono(telefono == null || telefono.isBlank() ? null : telefono);
        usuario.setActivo(true);
        usuario.setIntentosFallidosLogin(0);
        usuario.setFechaActualizacion(LocalDateTime.now());
        usuario.getRoles().add(rol);
        usuarioRepository.save(usuario);
        return usuario.getId();
    }

    // ─── Login ─────────────────────────────────────────────────

    @Transactional
    public AuthResponse login(LoginRequest req, String ipOrigen, String agenteUsuario) {
        String emailNormalizado = req.email() == null ? "" : req.email().trim().toLowerCase();

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(emailNormalizado, req.password())
        );
        UserDetails ud = (UserDetails) auth.getPrincipal();
        String token = jwtUtil.generarToken(ud.getUsername(), ud.getAuthorities());

        // Guardar sesión con hash del token
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailNormalizado)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));
        usuario.setIntentosFallidosLogin(0);
        usuario.setBloqueadoHasta(null);
        usuario.setFechaActualizacion(LocalDateTime.now());
        usuarioRepository.save(usuario);
        guardarSesion(usuario, token, ipOrigen, agenteUsuario);

        return AuthResponse.of(token, ud.getUsername());
    }

    // ─── Logout ────────────────────────────────────────────────

    @Transactional
    public void logout(String token) {
        String hash = hashToken(token);
        sesionRepository.findByTokenHashAndActivoTrue(hash).ifPresent(sesion -> {
            sesion.setActivo(false);
            sesion.setFechaRevocacion(LocalDateTime.now());
            sesion.setMotivoRevocacion("LOGOUT");
            sesionRepository.save(sesion);
        });
    }

    // ─── Verificar sesión activa ───────────────────────────────

    public boolean isSesionActiva(String token) {
        return validarYRenovarSesion(token) == SesionEstado.ACTIVA;
    }

    @Transactional
    public SesionEstado validarYRenovarSesion(String token) {
        String hash = hashToken(token);
        var sesionOpt = sesionRepository.findByTokenHash(hash);
        if (sesionOpt.isEmpty()) {
            return SesionEstado.NO_ENCONTRADA;
        }

        Sesion sesion = sesionOpt.get();
        if (!Boolean.TRUE.equals(sesion.getActivo())) {
            return SesionEstado.REVOCADA;
        }

        LocalDateTime ahora = LocalDateTime.now();
        if (sesion.getFechaExpiracion() == null || !sesion.getFechaExpiracion().isAfter(ahora)) {
            sesion.setActivo(false);
            sesion.setFechaRevocacion(ahora);
            sesion.setMotivoRevocacion("EXPIRADA_INACTIVIDAD");
            sesionRepository.save(sesion);
            return SesionEstado.EXPIRADA;
        }

        // Evita escribir en cada request para reducir contención y deadlocks.
        // Solo renueva cuando la sesión está próxima a vencer.
        long ventanaRenovacion = Math.max(1L, Math.min(5L, inactividadMinutos / 3L));
        LocalDateTime umbralRenovacion = ahora.plusMinutes(ventanaRenovacion);
        if (!sesion.getFechaExpiracion().isAfter(umbralRenovacion)) {
            sesion.setFechaExpiracion(ahora.plusMinutes(inactividadMinutos));
            sesionRepository.save(sesion);
        }
        return SesionEstado.ACTIVA;
    }

    // ─── Desactivación lógica de usuarios ──────────────────────

    @Transactional
    public void desactivarUsuario(Integer usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + usuarioId));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            return;
        }

        usuario.setActivo(false);
        usuario.setFechaDesactivacion(LocalDateTime.now());
        usuario.setFechaActualizacion(LocalDateTime.now());
        usuarioRepository.save(usuario);
        sesionRepository.desactivarSesionesActivasPorUsuarioId(
                usuarioId,
                LocalDateTime.now(),
                "USUARIO_DESACTIVADO"
        );
    }

    @Transactional
    public void activarUsuario(Integer usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + usuarioId));

        if (Boolean.TRUE.equals(usuario.getActivo())) {
            return;
        }

        usuario.setActivo(true);
        usuario.setFechaDesactivacion(null);
        usuario.setFechaActualizacion(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    // ─── Helpers ───────────────────────────────────────────────

    private void guardarSesion(Usuario usuario, String token, String ip, String agenteUsuario) {
        Sesion sesion = new Sesion();
        sesion.setUsuario(usuario);
        sesion.setTokenHash(hashToken(token));
        sesion.setIpOrigen(ip);
        sesion.setAgenteUsuario(agenteUsuario);
        sesion.setFechaExpiracion(LocalDateTime.now().plusMinutes(inactividadMinutos));
        sesionRepository.save(sesion);
    }

    /** SHA-256 hash del token JWT — nunca guardamos el token plano */
    public static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 no disponible", e);
        }
    }
}
