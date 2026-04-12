package pe.edu.utp.proyecto_integrador_casachantilly.operacion.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import pe.edu.utp.proyecto_integrador_casachantilly.operacion.servicio.AlertaOperativaStreamService;

@Tag(name = "Admin - Alertas Operativas", description = "Canal realtime para alertas del staff")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin/alertas")
@PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
public class AdminAlertaOperativaController {

    private final AlertaOperativaStreamService streamService;

    public AdminAlertaOperativaController(AlertaOperativaStreamService streamService) {
        this.streamService = streamService;
    }

    @Operation(summary = "Suscribirse al stream de alertas operativas (SSE)")
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return streamService.subscribe();
    }
}
