package pe.edu.utp.proyecto_integrador_casachantilly.notificacion.servicio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pe.edu.utp.proyecto_integrador_casachantilly.notificacion.entidad.Notificacion;

@Component
public class WhatsappNotificacionGatewayMock implements NotificacionCanalGateway {

    private static final Logger log = LoggerFactory.getLogger(WhatsappNotificacionGatewayMock.class);

    @Override
    public Notificacion.Canal getCanal() {
        return Notificacion.Canal.WHATSAPP;
    }

    @Override
    public boolean enviar(String destino, String asunto, String mensaje) {
        if (destino == null || destino.isBlank()) {
            return false;
        }
        log.info("[NOTIF MOCK][WHATSAPP] to={} subject={} message={}", destino, asunto, mensaje);
        return true;
    }
}
