package pe.edu.utp.proyecto_integrador_casachantilly.pedido.servicio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.pago.gateway", havingValue = "culqi-http")
public class CulqiHttpPagoGateway implements PagoGateway {

    private static final Logger log = LoggerFactory.getLogger(CulqiHttpPagoGateway.class);

    private final RestClient restClient;
    private final String secretKey;

    public CulqiHttpPagoGateway(
            @Value("${app.pago.culqi.base-url}") String baseUrl,
            @Value("${app.pago.culqi.secret-key}") String secretKey) {
        this.secretKey = secretKey;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public ResultadoCargo crearCargo(String tokenTarjeta, int montoCentimos, String email) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("Culqi secret key no configurada");
        }
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("amount", montoCentimos);
            payload.put("currency_code", "PEN");
            payload.put("email", email);
            payload.put("source_id", tokenTarjeta);
            payload.put("capture", true);

            Map<String, Object> response = restClient.post()
                    .uri("/charges")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                return new ResultadoCargo(false, "Respuesta vacia de pasarela", null, "CULQI_EMPTY_RESPONSE");
            }
            String referencia = String.valueOf(response.getOrDefault("id", ""));
            Object outcomeObj = response.get("outcome");
            boolean aprobado = true;
            if (outcomeObj instanceof Map<?, ?> outcome) {
                Object typeObj = outcome.containsKey("type") ? outcome.get("type") : "venta_exitosa";
                String type = String.valueOf(typeObj);
                aprobado = "venta_exitosa".equalsIgnoreCase(type);
            }
            String mensaje = aprobado ? "Pago procesado" : String.valueOf(response.getOrDefault("user_message", "Pago rechazado"));
            String codigoError = aprobado ? null : String.valueOf(response.getOrDefault("object", "CULQI_REJECTED"));
            return new ResultadoCargo(aprobado, mensaje, referencia, codigoError);
        } catch (Exception ex) {
            log.error("Error en Culqi HTTP gateway: {}", ex.getMessage());
            return new ResultadoCargo(false, "Error al procesar pago con proveedor", null, "CULQI_PROVIDER_ERROR");
        }
    }
}
