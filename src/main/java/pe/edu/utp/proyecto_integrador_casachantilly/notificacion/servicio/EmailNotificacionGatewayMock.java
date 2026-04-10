package pe.edu.utp.proyecto_integrador_casachantilly.notificacion.servicio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pe.edu.utp.proyecto_integrador_casachantilly.notificacion.entidad.Notificacion;

@Component
@ConditionalOnProperty(name = "app.notificacion.email.provider", havingValue = "mock", matchIfMissing = true)
public class EmailNotificacionGatewayMock implements NotificacionCanalGateway {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificacionGatewayMock.class);

    @Override
    public Notificacion.Canal getCanal() {
        return Notificacion.Canal.EMAIL;
    }

    @Override
    public boolean enviar(String destino, String asunto, String mensaje) {
        if (destino == null || destino.isBlank()) {
            return false;
        }
        log.info("[NOTIF MOCK][EMAIL] to={} subject={} message={}", destino, asunto, mensaje);
        return true;
    }
}
