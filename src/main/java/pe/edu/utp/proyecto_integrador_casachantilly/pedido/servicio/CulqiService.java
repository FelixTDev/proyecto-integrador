package pe.edu.utp.proyecto_integrador_casachantilly.pedido.servicio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio MOCK de la pasarela Culqi (Perú).
 * Simula la creación de cargos. En producción se reemplaza
 * con llamadas reales al SDK de Culqi.
 */
@Service
public class CulqiService {

    private static final Logger log = LoggerFactory.getLogger(CulqiService.class);

    /**
     * Simula un cargo a la pasarela Culqi.
     *
     * @param tokenTarjeta Token generado por Culqi.js en el frontend
     * @param montoCentimos Monto en céntimos de sol (ej: 1500 = S/ 15.00)
     * @param email Email del cliente
     * @return Mapa con resultado de la operación
     */
    public Map<String, Object> crearCargo(String tokenTarjeta, int montoCentimos, String email) {
        log.info("[CULQI MOCK] Procesando cargo: token={}, monto={} céntimos, email={}",
                tokenTarjeta.substring(0, Math.min(8, tokenTarjeta.length())) + "***",
                montoCentimos, email);

        if (tokenTarjeta.toLowerCase().startsWith("fail")) {
            log.warn("[CULQI MOCK] Cargo RECHAZADO");
            return Map.of(
                    "aprobado", false,
                    "mensaje", "Tarjeta rechazada por el banco emisor",
                    "referencia", "culqi_err_" + UUID.randomUUID().toString().substring(0, 8)
            );
        }

        if (montoCentimos > 1_000_000) {
            log.warn("[CULQI MOCK] Cargo RECHAZADO — monto excede límite");
            return Map.of(
                    "aprobado", false,
                    "mensaje", "Monto excede el límite permitido por la tarjeta",
                    "referencia", "culqi_limit_" + UUID.randomUUID().toString().substring(0, 8)
            );
        }

        String referencia = "culqi_ok_" + UUID.randomUUID().toString().substring(0, 12);
        log.info("[CULQI MOCK] Cargo APROBADO — ref: {}", referencia);

        return Map.of(
                "aprobado", true,
                "mensaje", "Cargo procesado exitosamente",
                "referencia", referencia
        );
    }
}
