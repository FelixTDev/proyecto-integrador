package pe.edu.utp.proyecto_integrador_casachantilly.personalizacion.dto;

public record PersonalizacionRequest(
    String saborBizcocho, String tipoRelleno, String colorDecorado,
    String textoPastel, String notasCliente, String imagenReferenciaUrl
) {}
