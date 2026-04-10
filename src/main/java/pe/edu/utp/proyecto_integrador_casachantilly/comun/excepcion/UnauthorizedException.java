package pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
