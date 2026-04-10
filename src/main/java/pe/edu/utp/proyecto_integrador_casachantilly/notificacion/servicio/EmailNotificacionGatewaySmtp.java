package pe.edu.utp.proyecto_integrador_casachantilly.notificacion.servicio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import pe.edu.utp.proyecto_integrador_casachantilly.notificacion.entidad.Notificacion;

@Component
@ConditionalOnProperty(name = "app.notificacion.email.provider", havingValue = "smtp")
public class EmailNotificacionGatewaySmtp implements NotificacionCanalGateway {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificacionGatewaySmtp.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailNotificacionGatewaySmtp(
            JavaMailSender mailSender,
            @Value("${app.notificacion.email.from}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public Notificacion.Canal getCanal() {
        return Notificacion.Canal.EMAIL;
    }

    @Override
    public boolean enviar(String destino, String asunto, String mensaje) {
        if (destino == null || destino.isBlank()) {
            return false;
        }
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(fromAddress);
            mail.setTo(destino);
            mail.setSubject(asunto == null ? "Notificacion" : asunto);
            mail.setText(mensaje == null ? "" : mensaje);
            mailSender.send(mail);
            return true;
        } catch (Exception ex) {
            log.error("Error enviando email SMTP: {}", ex.getMessage());
            return false;
        }
    }
}
