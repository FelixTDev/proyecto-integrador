package pe.edu.utp.proyecto_integrador_casachantilly.auth.servicio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.dto.AdminCrearEmpleadoRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.dto.AdminEmpleadoListItemResponse;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.dto.AuthMeResponse;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.dto.AuthResponse;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.dto.LoginRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.dto.RegistroRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.dto.SocialLoginRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.entidad.Rol;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.entidad.Sesion;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.entidad.Usuario;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.repositorio.RolRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.repositorio.SesionRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.repositorio.UsuarioRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.BadRequestException;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.ResourceNotFoundException;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.UnauthorizedException;
import pe.edu.utp.proyecto_integrador_casachantilly.config.JwtUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    public enum SesionEstado {
        ACTIVA, EXPIRADA, REVOCADA, NO_ENCONTRADA
    }

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private SesionRepository sesionRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UsuarioDetailsService usuarioDetailsService;

    @org.springframework.beans.factory.annotation.Value("${app.session.inactivity-minutes:30}")
    private long inactividadMinutos;

    public UsuarioRepository getUsuarioRepository() {
        return usuarioRepository;
    }

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
            throw new BadRequestException("La contrasena debe tener al menos 6 caracteres");
        }
        if (telefono != null && !telefono.isBlank() && !telefono.matches("^\\d{9}$")) {
            throw new BadRequestException("El numero de celular debe tener 9 digitos");
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

        UserDetails ud = usuarioDetailsService.loadUserByUsername(email);
        String token = jwtUtil.generarToken(ud.getUsername(), ud.getAuthorities());
        guardarSesion(usuario, token, null, null);
        return construirAuthResponse(token, email, extraerRoles(ud));
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
            throw new BadRequestException("La contrasena debe tener al menos 6 caracteres");
        }
        if (telefono != null && !telefono.isBlank() && !telefono.matches("^\\d{9}$")) {
            throw new BadRequestException("El numero de celular debe tener 9 digitos");
        }
        if (!"ADMIN".equals(rolNombre) && !"VENDEDOR".equals(rolNombre)) {
            throw new BadRequestException("Rol invalido. Use ADMIN o VENDEDOR");
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

    @Transactional(readOnly = true)
    public List<AdminEmpleadoListItemResponse> listarEmpleadosAdministrables() {
        return usuarioRepository.findAll().stream()
                .filter(usuario -> usuario.getRoles().stream()
                        .map(Rol::getNombre)
                        .anyMatch(nombre -> "ADMIN".equals(nombre) || "VENDEDOR".equals(nombre)))
                .sorted(Comparator.comparing(Usuario::getFechaCreacion).reversed())
                .map(usuario -> new AdminEmpleadoListItemResponse(
                        usuario.getId(),
                        usuario.getNombre(),
                        usuario.getEmail(),
                        rolPrincipalDesdeEntidades(usuario),
                        Boolean.TRUE.equals(usuario.getActivo()),
                        usuario.getFechaCreacion()))
                .toList();
    }

    @Transactional
    public AuthResponse login(LoginRequest req, String ipOrigen, String agenteUsuario) {
        String emailNormalizado = req.email() == null ? "" : req.email().trim().toLowerCase();

        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(emailNormalizado, req.password())
            );
        } catch (AuthenticationException ex) {
            throw new UnauthorizedException("Credenciales invalidas");
        }

        UserDetails ud = (UserDetails) auth.getPrincipal();
        String token = jwtUtil.generarToken(ud.getUsername(), ud.getAuthorities());

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailNormalizado)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));
        usuario.setIntentosFallidosLogin(0);
        usuario.setBloqueadoHasta(null);
        usuario.setFechaActualizacion(LocalDateTime.now());
        usuarioRepository.save(usuario);
        guardarSesion(usuario, token, ipOrigen, agenteUsuario);

        return construirAuthResponse(token, ud.getUsername(), extraerRoles(ud));
    }

    @Transactional
    public AuthResponse loginSocial(SocialLoginRequest req, String proveedor, String ipOrigen, String agenteUsuario) {
        String emailNormalizado = req.email() == null ? "" : req.email().trim().toLowerCase();
        String nombreLimpio = req.nombre() == null ? "" : req.nombre().trim();
        String oauthIdLimpio = req.oauthId() == null ? "" : req.oauthId().trim();
        String proveedorNormalizado = proveedor == null ? "" : proveedor.trim().toUpperCase();

        if (!"GOOGLE".equals(proveedorNormalizado) && !"FACEBOOK".equals(proveedorNormalizado)) {
            throw new BadRequestException("Proveedor social no soportado");
        }
        if (emailNormalizado.isBlank() || nombreLimpio.isBlank() || oauthIdLimpio.isBlank()) {
            throw new BadRequestException("Datos sociales incompletos");
        }

        Rol rolCliente = rolRepository.findByNombre("CLIENTE")
                .orElseThrow(() -> new BadRequestException("Rol CLIENTE no encontrado en BD"));

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailNormalizado)
                .orElseGet(() -> {
                    Usuario nuevo = new Usuario();
                    nuevo.setEmail(emailNormalizado);
                    nuevo.setNombre(nombreLimpio);
                    nuevo.setActivo(true);
                    nuevo.setIntentosFallidosLogin(0);
                    nuevo.setFechaActualizacion(LocalDateTime.now());
                    nuevo.getRoles().add(rolCliente);
                    return usuarioRepository.save(nuevo);
                });

        usuario.setNombre(nombreLimpio);
        usuario.setProveedorOauth(proveedorNormalizado);
        usuario.setOauthId(oauthIdLimpio);
        usuario.setFechaActualizacion(LocalDateTime.now());
        if (usuario.getRoles().isEmpty()) {
            usuario.getRoles().add(rolCliente);
        }
        usuarioRepository.save(usuario);

        UserDetails ud = usuarioDetailsService.loadUserByUsername(emailNormalizado);
        String token = jwtUtil.generarToken(ud.getUsername(), ud.getAuthorities());
        guardarSesion(usuario, token, ipOrigen, agenteUsuario);
        return construirAuthResponse(token, emailNormalizado, extraerRoles(ud));
    }

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

    @Transactional
    public AuthResponse refreshToken(String bearerToken, String ipOrigen, String agenteUsuario) {
        if (bearerToken == null || bearerToken.isBlank()) {
            throw new UnauthorizedException("Token requerido");
        }
        String token = bearerToken.startsWith("Bearer ") ? bearerToken.substring(7) : bearerToken;
        if (!jwtUtil.isTokenValid(token)) {
            throw new UnauthorizedException("Token invalido o expirado");
        }

        SesionEstado estado = validarYRenovarSesion(token);
        if (estado != SesionEstado.ACTIVA) {
            throw new UnauthorizedException("Sesion no valida");
        }

        String email = jwtUtil.extractEmail(token);
        UserDetails ud = usuarioDetailsService.loadUserByUsername(email);
        String nuevoToken = jwtUtil.generarToken(ud.getUsername(), ud.getAuthorities());
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        String hashActual = hashToken(token);
        sesionRepository.findByTokenHashAndActivoTrue(hashActual).ifPresent(sesion -> {
            sesion.setActivo(false);
            sesion.setFechaRevocacion(LocalDateTime.now());
            sesion.setMotivoRevocacion("REFRESH_ROTATION");
            sesionRepository.save(sesion);
        });

        guardarSesion(usuario, nuevoToken, ipOrigen, agenteUsuario);
        return construirAuthResponse(nuevoToken, email, extraerRoles(ud));
    }

    @Transactional(readOnly = true)
    public AuthMeResponse obtenerPerfil(String email) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<String> roles = usuario.getRoles().stream()
                .map(Rol::getNombre)
                .map(nombre -> nombre == null ? "" : nombre.trim().toUpperCase())
                .filter(nombre -> !nombre.isBlank())
                .distinct()
                .sorted(Comparator.comparingInt(this::prioridadRol))
                .toList();

        String primaryRole = seleccionarRolPrimario(roles);
        return new AuthMeResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getTelefono(),
                primaryRole,
                primaryRole,
                roles
        );
    }

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

        long ventanaRenovacion = Math.max(1L, Math.min(5L, inactividadMinutos / 3L));
        LocalDateTime umbralRenovacion = ahora.plusMinutes(ventanaRenovacion);
        if (!sesion.getFechaExpiracion().isAfter(umbralRenovacion)) {
            sesion.setFechaExpiracion(ahora.plusMinutes(inactividadMinutos));
            sesionRepository.save(sesion);
        }
        return SesionEstado.ACTIVA;
    }

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

    @Transactional
    public void solicitarRecuperacionPassword(String email) {
        String emailNormalizado = email.trim().toLowerCase();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailIgnoreCase(emailNormalizado);

        if (usuarioOpt.isEmpty()) {
            return;
        }

        Usuario usuario = usuarioOpt.get();
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            return;
        }

        String token = UUID.randomUUID().toString();
        usuario.setResetToken(token);
        usuario.setResetTokenExpiration(LocalDateTime.now().plusHours(1));
        usuarioRepository.save(usuario);

        log.info("Solicitud de recuperacion de password registrada para {}", emailNormalizado);
    }

    @Transactional
    public void resetPassword(String token, String nuevaPassword) {
        Usuario usuario = usuarioRepository.findByResetToken(token)
                .orElseThrow(() -> new BadRequestException("Token invalido o expirado"));

        if (usuario.getResetTokenExpiration() == null || usuario.getResetTokenExpiration().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("El token ha expirado");
        }

        usuario.setPasswordHash(passwordEncoder.encode(nuevaPassword));
        usuario.setResetToken(null);
        usuario.setResetTokenExpiration(null);
        usuario.setFechaActualizacion(LocalDateTime.now());
        usuario.setIntentosFallidosLogin(0);
        usuario.setBloqueadoHasta(null);
        usuarioRepository.save(usuario);

        sesionRepository.desactivarSesionesActivasPorUsuarioId(
                usuario.getId(),
                LocalDateTime.now(),
                "CAMBIO_PASSWORD"
        );
    }

    private void guardarSesion(Usuario usuario, String token, String ip, String agenteUsuario) {
        Sesion sesion = new Sesion();
        sesion.setUsuario(usuario);
        sesion.setTokenHash(hashToken(token));
        sesion.setIpOrigen(ip);
        sesion.setAgenteUsuario(agenteUsuario);
        sesion.setFechaExpiracion(LocalDateTime.now().plusMinutes(inactividadMinutos));
        sesionRepository.save(sesion);
    }

    private List<String> extraerRoles(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority() == null ? "" : authority.getAuthority().toUpperCase())
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .filter(role -> !role.isBlank())
                .distinct()
                .toList();
    }

    private AuthResponse construirAuthResponse(String token, String email, List<String> roles) {
        List<String> orderedRoles = roles == null ? List.of() : roles.stream()
                .map(role -> role == null ? "" : role.trim().toUpperCase())
                .filter(role -> !role.isBlank())
                .distinct()
                .sorted(Comparator.comparingInt(this::prioridadRol))
                .toList();
        return AuthResponse.of(token, email, orderedRoles);
    }

    private int prioridadRol(String role) {
        return switch (role) {
            case "ADMIN" -> 0;
            case "VENDEDOR" -> 1;
            case "CLIENTE" -> 2;
            default -> 9;
        };
    }

    private String seleccionarRolPrimario(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return "CLIENTE";
        }
        return roles.stream()
                .sorted(Comparator.comparingInt(this::prioridadRol))
                .findFirst()
                .orElse("CLIENTE");
    }

    private String rolPrincipalDesdeEntidades(Usuario usuario) {
        List<String> roles = usuario.getRoles().stream()
                .map(Rol::getNombre)
                .map(nombre -> nombre == null ? "" : nombre.trim().toUpperCase())
                .toList();
        return seleccionarRolPrimario(roles);
    }

    /** SHA-256 hash del token JWT. Nunca guardamos el token plano. */
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