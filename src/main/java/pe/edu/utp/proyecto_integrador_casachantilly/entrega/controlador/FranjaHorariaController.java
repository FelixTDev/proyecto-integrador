package pe.edu.utp.proyecto_integrador_casachantilly.entrega.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.dto.ApiResponse;
import pe.edu.utp.proyecto_integrador_casachantilly.entrega.dto.FranjaHorariaDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.entrega.servicio.FranjaHorariaService;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Entregas", description = "Franjas horarias para entrega/recojo")
@RestController
@RequestMapping("/api/entregas")
public class FranjaHorariaController {

    @Autowired private FranjaHorariaService franjaService;

    @Operation(summary = "Listar franjas horarias disponibles por fecha")
    @GetMapping("/franjas")
    public ResponseEntity<ApiResponse<List<FranjaHorariaDTO>>> listarPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(ApiResponse.ok("Franjas disponibles", franjaService.listarDisponiblesPorFecha(fecha)));
    }

    @Operation(summary = "Listar todas las franjas futuras")
    @GetMapping("/franjas/futuras")
    public ResponseEntity<ApiResponse<List<FranjaHorariaDTO>>> futuras() {
        return ResponseEntity.ok(ApiResponse.ok("Franjas futuras", franjaService.listarFuturas()));
    }
}
