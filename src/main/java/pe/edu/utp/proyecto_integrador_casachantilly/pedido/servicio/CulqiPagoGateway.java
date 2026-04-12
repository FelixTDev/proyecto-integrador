package pe.edu.utp.proyecto_integrador_casachantilly.pedido.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.pago.gateway", havingValue = "mock", matchIfMissing = true)
public class CulqiPagoGateway implements PagoGateway {

    @Autowired
    private CulqiService culqiService;

    @Override
    public ResultadoCargo crearCargo(String tokenTarjeta, int montoCentimos, String email) {
        Map<String, Object> response = culqiService.crearCargo(tokenTarjeta, montoCentimos, email);
        boolean aprobado = Boolean.TRUE.equals(response.get("aprobado"));
        String mensaje = String.valueOf(response.getOrDefault("mensaje", ""));
        String referencia = String.valueOf(response.getOrDefault("referencia", ""));
        String codigoError = aprobado ? null : String.valueOf(response.getOrDefault("codigoError", "PAGO_RECHAZADO"));
        return new ResultadoCargo(aprobado, mensaje, referencia, codigoError);
    }
}
