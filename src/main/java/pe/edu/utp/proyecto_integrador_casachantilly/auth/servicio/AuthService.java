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
import pe.edu.utp.proyecto_integrador_casachantilly.auth.dto.LoginRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.dto.RegistroRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.entidad.Rol;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.entidad.Sesion;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.entidad.Usuario;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.repositorio.RolRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.repositorio.SesionRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.repositorio.UsuarioRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.BadRequestException;
import pe.edu.utp.proyecto_integrador_casachantilly.config.JwtUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class AuthService {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private SesionRepository sesionRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UsuarioDetailsService usuarioDetailsService;

    // ─── Registro ──────────────────────────────────────────────

    @Transactional
    public AuthResponse registro(RegistroRequest req) {
        if (usuarioRepository.findByEmail(req.email()).isPresent()) {
            throw new BadRequestException("Ya existe un usuario con el email: " + req.email());
        }

        Rol rolCliente = rolRepository.findByNombre("CLIENTE")
                .orElseThrow(() -> new BadRequestException("Rol CLIENTE no encontrado en BD"));

        Usuario usuario = new Usuario();
        usuario.setNombre(req.nombre());
        usuario.setEmail(req.email());
        usuario.setPasswordHash(passwordEncoder.encode(req.password()));
        usuario.getRoles().add(rolCliente);
        usuarioRepository.save(usuario);

        // Generar token
        UserDetails ud = usuarioDetailsService.loadUserByUsername(req.email());
        String token = jwtUtil.generarToken(ud.getUsername(), ud.getAuthorities());

        // Guardar sesión
        guardarSesion(usuario, token, null);

        return AuthResponse.of(token, usuario.getEmail());
    }

    // ─── Login ─────────────────────────────────────────────────

    @Transactional
    public AuthResponse login(LoginRequest req, String ipOrigen) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );
        UserDetails ud = (UserDetails) auth.getPrincipal();
        String token = jwtUtil.generarToken(ud.getUsername(), ud.getAuthorities());

        // Guardar sesión con hash del token
        Usuario usuario = usuarioRepository.findByEmail(req.email())
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));
        guardarSesion(usuario, token, ipOrigen);

        return AuthResponse.of(token, ud.getUsername());
    }

    // ─── Logout ────────────────────────────────────────────────

    @Transactional
    public void logout(String token) {
        String hash = hashToken(token);
        sesionRepository.findByTokenHashAndActivoTrue(hash).ifPresent(sesion -> {
            sesion.setActivo(false);
            sesionRepository.save(sesion);
        });
    }

    // ─── Verificar sesión activa ───────────────────────────────

    public boolean isSesionActiva(String token) {
        String hash = hashToken(token);
        return sesionRepository.findByTokenHashAndActivoTrue(hash).isPresent();
    }

    // ─── Helpers ───────────────────────────────────────────────

    private void guardarSesion(Usuario usuario, String token, String ip) {
        Sesion sesion = new Sesion();
        sesion.setUsuario(usuario);
        sesion.setTokenHash(hashToken(token));
        sesion.setIpOrigen(ip);
        sesion.setFechaExpiracion(LocalDateTime.now().plusHours(24));
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
