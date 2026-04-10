package pe.edu.utp.proyecto_integrador_casachantilly.pedido.servicio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.repositorio.PedidoRepository;

import java.time.LocalDate;

@Service
public class CapacidadProduccionService {

    private final PedidoRepository pedidoRepository;
    private final int maxPedidosOperativosPorDia;

    public CapacidadProduccionService(
            PedidoRepository pedidoRepository,
            @Value("${app.operacion.max-pedidos-dia:200}") int maxPedidosOperativosPorDia) {
        this.pedidoRepository = pedidoRepository;
        this.maxPedidosOperativosPorDia = Math.max(1, maxPedidosOperativosPorDia);
    }

    @Transactional(readOnly = true)
    public boolean hayCapacidadParaFecha(LocalDate fecha) {
        long pedidosOperativos = pedidoRepository.countPedidosOperativosPorFecha(fecha);
        return pedidosOperativos < maxPedidosOperativosPorDia;
    }

    @Transactional(readOnly = true)
    public int getMaxPedidosOperativosPorDia() {
        return maxPedidosOperativosPorDia;
    }
}
