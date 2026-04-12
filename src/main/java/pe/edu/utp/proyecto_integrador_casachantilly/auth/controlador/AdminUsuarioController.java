package pe.edu.utp.proyecto_integrador_casachantilly.auth.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.dto.AdminCrearEmpleadoRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.dto.AdminEmpleadoListItemResponse;
import pe.edu.utp.proyecto_integrador_casachantilly.auth.servicio.AuthService;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.dto.ApiResponse;

import java.util.List;
import java.util.Map;

@Tag(name = "Admin — Usuarios", description = "Gestión de estado lógico de usuarios")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsuarioController {

    @Autowired
    private AuthService authService;

    @Operation(summary = "Crear nuevo empleado administrativo (solo ADMIN)")
    @PostMapping("/empleados")
    public ResponseEntity<ApiResponse<Map<String, Object>>> crearEmpleado(
            @Valid @RequestBody AdminCrearEmpleadoRequest request) {
        Integer id = authService.crearEmpleado(request);
        return ResponseEntity.ok(ApiResponse.ok(
                "Empleado creado",
                Map.of("id", id, "email", request.email(), "rol", request.rol().toUpperCase())
        ));
    }

    @Operation(summary = "Listar empleados administrables (rol ADMIN)")
    @GetMapping("/empleados")
    public ResponseEntity<ApiResponse<List<AdminEmpleadoListItemResponse>>> listarEmpleados() {
        return ResponseEntity.ok(ApiResponse.ok(
                "Empleados administrativos",
                authService.listarEmpleadosAdministrables()
        ));
    }

    @Operation(summary = "Desactivar usuario (baja lógica)")
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ApiResponse<Map<String, Object>>> desactivar(@PathVariable Integer id) {
        authService.desactivarUsuario(id);
        return ResponseEntity.ok(ApiResponse.ok(
                "Usuario desactivado",
                Map.of("id", id, "activo", false)
        ));
    }

    @Operation(summary = "Activar usuario")
    @PatchMapping("/{id}/activar")
    public ResponseEntity<ApiResponse<Map<String, Object>>> activar(@PathVariable Integer id) {
        authService.activarUsuario(id);
        return ResponseEntity.ok(ApiResponse.ok(
                "Usuario activado",
                Map.of("id", id, "activo", true)
        ));
    }
}
