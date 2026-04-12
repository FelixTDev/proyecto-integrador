package pe.edu.utp.proyecto_integrador_casachantilly.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pe.edu.utp.proyecto_integrador_casachantilly.notificacion.servicio.NotificacionService;

import java.util.List;
import java.util.Map;

@Component
public class AlertaOperativaScheduler {

    private static final Logger log = LoggerFactory.getLogger(AlertaOperativaScheduler.class);
    private static final int UMBRAL_STOCK_BAJO = 5;
    private long ultimoConteoUrgentesNotificado = -1L;

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private NotificacionService notificacionService;

    @Scheduled(fixedRate = 3600_000)
    public void verificarStockBajo() {
        try {
            List<Map<String, Object>> bajosStock = jdbcTemplate.queryForList(
                    """
                    SELECT pv.id, p.nombre, pv.nombre_variante, pv.stock_disponible
                      FROM producto_variante pv
                      JOIN producto p ON p.id = pv.producto_id
                     WHERE pv.activo = 1 AND pv.stock_disponible > 0 AND pv.stock_disponible <= ?
                    """, UMBRAL_STOCK_BAJO
            );

            if (!bajosStock.isEmpty()) {
                StringBuilder msg = new StringBuilder("Productos con stock bajo:\n");
                for (Map<String, Object> row : bajosStock) {
                    msg.append("- ").append(row.get("nombre"))
                       .append(" (").append(row.get("nombre_variante")).append(")")
                       .append(": ").append(row.get("stock_disponible")).append(" uds\n");
                }

                List<Integer> adminIds = jdbcTemplate.queryForList(
                        "SELECT ur.usuario_id FROM usuario_rol ur JOIN rol r ON r.id = ur.rol_id WHERE r.nombre = 'ADMIN'",
                        Integer.class
                );

                for (Integer adminId : adminIds) {
                    notificacionService.registrarEventoPedido(adminId, null,
                            "Alerta: Stock bajo", msg.toString());
                }
                log.info("Alerta de stock bajo enviada a {} administradores para {} productos",
                        adminIds.size(), bajosStock.size());
            }
        } catch (Exception e) {
            log.warn("Error en verificación de stock bajo: {}", e.getMessage());
        }
    }

    @Scheduled(fixedRate = 1800_000)
    public void verificarRecojosProximos() {
        try {
            long recojosProximos = 0;
            try {
                recojosProximos = jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*) FROM pedido p
                          JOIN franja_horaria fh ON fh.id = p.franja_horaria_id
                         WHERE p.es_recojo_tienda = 1
                           AND p.estado_actual_id IN (2, 3, 4)
                           AND fh.fecha = CURDATE()
                           AND fh.hora_inicio <= ADDTIME(CURTIME(), '01:00:00')
                           AND fh.hora_inicio >= CURTIME()
                        """, Long.class
                );
            } catch (Exception ignored) {
                return;
            }

            if (recojosProximos > 0) {
                List<Integer> adminIds = jdbcTemplate.queryForList(
                        "SELECT ur.usuario_id FROM usuario_rol ur JOIN rol r ON r.id = ur.rol_id WHERE r.nombre = 'ADMIN'",
                        Integer.class
                );
                for (Integer adminId : adminIds) {
                    notificacionService.registrarEventoPedido(adminId, null,
                            "Recojo próximo", recojosProximos + " pedido(s) de recojo en la próxima hora.");
                }
                log.info("Alerta de recojo próximo: {} pedidos", recojosProximos);
            }
        } catch (Exception e) {
            log.warn("Error en verificación de recojos: {}", e.getMessage());
        }
    }

    @Scheduled(fixedRate = 900_000)
    public void verificarPedidosUrgentesNuevos() {
        try {
            Long urgentes = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*)
                      FROM pedido p
                     WHERE p.estado_actual_id IN (1,2)
                       AND p.fecha_creacion >= DATE_SUB(NOW(), INTERVAL 30 MINUTE)
                    """, Long.class
            );
            long totalUrgentes = urgentes != null ? urgentes : 0L;
            if (totalUrgentes <= 0 || totalUrgentes == ultimoConteoUrgentesNotificado) {
                return;
            }

            List<Integer> adminIds = jdbcTemplate.queryForList(
                    "SELECT ur.usuario_id FROM usuario_rol ur JOIN rol r ON r.id = ur.rol_id WHERE r.nombre = 'ADMIN'",
                    Integer.class
            );
            for (Integer adminId : adminIds) {
                notificacionService.registrarEventoPedido(adminId, null,
                        "Pedidos urgentes en revisión",
                        "Hay " + totalUrgentes + " pedido(s) nuevos/urgentes en los últimos 30 minutos.");
            }
            ultimoConteoUrgentesNotificado = totalUrgentes;
            log.info("Alerta de pedidos urgentes enviada: {}", totalUrgentes);
        } catch (Exception e) {
            log.warn("Error en verificación de pedidos urgentes: {}", e.getMessage());
        }
    }
}
