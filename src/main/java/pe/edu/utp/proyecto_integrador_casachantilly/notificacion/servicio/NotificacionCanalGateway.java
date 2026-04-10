package pe.edu.utp.proyecto_integrador_casachantilly.notificacion.servicio;

import pe.edu.utp.proyecto_integrador_casachantilly.notificacion.entidad.Notificacion;

public interface NotificacionCanalGateway {
    Notificacion.Canal getCanal();
    boolean enviar(String destino, String asunto, String mensaje);
}
