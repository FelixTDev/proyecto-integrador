package pe.edu.utp.proyecto_integrador_casachantilly.pedido.servicio;

public interface PagoGateway {
    ResultadoCargo crearCargo(String tokenTarjeta, int montoCentimos, String email);

    record ResultadoCargo(boolean aprobado, String mensaje, String referencia, String codigoError) {}
}
