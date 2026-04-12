package pe.edu.utp.proyecto_integrador_casachantilly.admin.dto;

public record TopProductoDTO(
        String producto,
        String categoria,
        Long unidadesVendidas,
        Integer rankingUnidades
) {
}