package pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion;

public class TooManyRequestsException extends RuntimeException {
    public TooManyRequestsException(String message) {
        super(message);
    }
}
