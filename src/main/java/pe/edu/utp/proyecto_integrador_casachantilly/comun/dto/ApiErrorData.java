package pe.edu.utp.proyecto_integrador_casachantilly.comun.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public record ApiErrorData(
        String code,
        String path,
        OffsetDateTime timestamp,
        Map<String, Object> details
) {
    public static ApiErrorData of(String code, String path) {
        return new ApiErrorData(code, path, OffsetDateTime.now(), Map.of());
    }

    public static ApiErrorData of(String code, String path, Map<String, Object> details) {
        return new ApiErrorData(code, path, OffsetDateTime.now(), details == null ? Map.of() : details);
    }
}
