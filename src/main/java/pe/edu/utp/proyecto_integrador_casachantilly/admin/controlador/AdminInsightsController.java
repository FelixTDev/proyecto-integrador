package pe.edu.utp.proyecto_integrador_casachantilly.admin.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.dto.ApiResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Admin — Insights", description = "Indicadores y analítica para dashboard/reportes/promociones")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin/insights")
@PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
public class AdminInsightsController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Operation(summary = "KPIs y series para reportes comerciales")
    @GetMapping("/reportes")
    public ApiResponse<Map<String, Object>> reportes(@RequestParam(defaultValue = "14") int days) {
        int rango = Math.max(1, Math.min(days, 365));
        LocalDate desde = LocalDate.now().minusDays(rango - 1L);

        BigDecimal ventasBrutas = queryDecimal(
                "select coalesce(sum(ingresos_brutos),0) from vw_ventas_diarias where fecha >= ?",
                java.sql.Date.valueOf(desde)
        );
        long pedidosTotales = queryLong(
                "select coalesce(sum(total_pedidos),0) from vw_ventas_diarias where fecha >= ?",
                java.sql.Date.valueOf(desde)
        );
        long pedidosCompletados = queryLong(
                "select coalesce(sum(pedidos_entregados),0) from vw_ventas_diarias where fecha >= ?",
                java.sql.Date.valueOf(desde)
        );
        BigDecimal ticketPromedio = pedidosTotales > 0
                ? ventasBrutas.divide(BigDecimal.valueOf(pedidosTotales), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        long cancelados = queryLong(
                "select count(*) from pedido p where p.fecha_creacion >= ? and p.estado_actual_id in (7,8)",
                java.sql.Date.valueOf(desde)
        );
        double cancelPct = pedidosTotales > 0 ? (cancelados * 100.0 / pedidosTotales) : 0.0;

        List<Map<String, Object>> ventasDiarias = jdbcTemplate.queryForList(
                """
                select fecha, total_pedidos, coalesce(ingresos_brutos,0) as ingresos_brutos
                  from vw_ventas_diarias
                 where fecha >= ?
                 order by fecha asc
                """,
                java.sql.Date.valueOf(desde)
        );

        long delivery = queryLong(
                "select count(*) from pedido p where p.fecha_creacion >= ? and coalesce(p.es_recojo_tienda,0)=0",
                java.sql.Date.valueOf(desde)
        );
        long recojo = queryLong(
                "select count(*) from pedido p where p.fecha_creacion >= ? and coalesce(p.es_recojo_tienda,0)=1",
                java.sql.Date.valueOf(desde)
        );
        long totalEntrega = delivery + recojo;
        double deliveryPct = totalEntrega > 0 ? (delivery * 100.0 / totalEntrega) : 0.0;
        double recojoPct = totalEntrega > 0 ? (recojo * 100.0 / totalEntrega) : 0.0;

        List<Map<String, Object>> metodos = jdbcTemplate.queryForList(
                """
                select metodo, total_transacciones, coalesce(monto_total,0) as monto_total, coalesce(porcentaje_uso,0) as porcentaje_uso
                  from vw_metodos_pago_stats
                 order by total_transacciones desc
                """
        );

        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("ventasBrutas", ventasBrutas);
        kpis.put("pedidosCompletados", pedidosCompletados);
        kpis.put("ticketPromedio", ticketPromedio);
        kpis.put("cancelacionesPct", BigDecimal.valueOf(cancelPct).setScale(2, RoundingMode.HALF_UP));
        kpis.put("pedidosTotales", pedidosTotales);

        Map<String, Object> tiposEntrega = new LinkedHashMap<>();
        tiposEntrega.put("deliveryPct", BigDecimal.valueOf(deliveryPct).setScale(2, RoundingMode.HALF_UP));
        tiposEntrega.put("recojoPct", BigDecimal.valueOf(recojoPct).setScale(2, RoundingMode.HALF_UP));
        tiposEntrega.put("deliveryCount", delivery);
        tiposEntrega.put("recojoCount", recojo);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("kpis", kpis);
        data.put("ventasDiarias", ventasDiarias);
        data.put("tiposEntrega", tiposEntrega);
        data.put("metodosPago", metodos);
        data.put("rangoDias", rango);
        return ApiResponse.ok("Reportes generados", data);
    }

    @Operation(summary = "Análisis de productos y demanda horaria")
    @GetMapping("/analisis")
    public ApiResponse<Map<String, Object>> analisis(@RequestParam(defaultValue = "30") int days) {
        int rango = Math.max(1, Math.min(days, 365));

        List<Map<String, Object>> topProductos = jdbcTemplate.queryForList(
                """
                select producto, categoria, coalesce(unidades_vendidas,0) as unidades_vendidas, ranking_unidades
                  from vw_productos_top
                 order by ranking_unidades asc
                 limit 5
                """
        );

        List<Map<String, Object>> horas = jdbcTemplate.queryForList(
                """
                select hora, num_pedidos
                  from vw_horas_pico
                 order by num_pedidos desc, hora asc
                """
        );

        long totalPedidosHora = horas.stream().mapToLong(m -> toLong(m.get("num_pedidos"))).sum();
        long manana = 0L;
        long tarde = 0L;
        long noche = 0L;
        for (Map<String, Object> row : horas) {
            int h = toInt(row.get("hora"));
            long n = toLong(row.get("num_pedidos"));
            if (h >= 9 && h <= 12) manana += n;
            else if (h >= 13 && h <= 17) tarde += n;
            else if (h >= 18 && h <= 21) noche += n;
        }

        List<Map<String, Object>> horasPico = new ArrayList<>();
        horasPico.add(buildFranja("Mañana", "9:00am - 12:00pm", manana, totalPedidosHora));
        horasPico.add(buildFranja("Tarde (Pico)", "1:00pm - 5:00pm", tarde, totalPedidosHora));
        horasPico.add(buildFranja("Noche", "6:00pm - 9:00pm", noche, totalPedidosHora));

        List<Map<String, Object>> rotacionCategorias = jdbcTemplate.queryForList(
                """
                select categoria, coalesce(sum(unidades_vendidas),0) as unidades
                  from vw_productos_top
                 group by categoria
                 order by unidades desc
                """
        );
        long totalUnidades = rotacionCategorias.stream().mapToLong(m -> toLong(m.get("unidades"))).sum();
        for (Map<String, Object> cat : rotacionCategorias) {
            long unidades = toLong(cat.get("unidades"));
            BigDecimal pct = totalUnidades > 0
                    ? BigDecimal.valueOf(unidades * 100.0 / totalUnidades).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            cat.put("porcentaje", pct);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("topProductos", topProductos);
        data.put("horasPico", horasPico);
        data.put("rotacionCategorias", rotacionCategorias);
        data.put("rangoDias", rango);
        return ApiResponse.ok("Análisis generado", data);
    }

    @Operation(summary = "Listado de promociones registradas")
    @GetMapping("/promociones")
    public ApiResponse<List<Map<String, Object>>> promociones() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                """
                select p.id, p.nombre, p.tipo_descuento, p.valor_descuento, p.monto_minimo, p.aplica_a,
                       p.fecha_inicio, p.fecha_fin, p.activo, p.codigo_cupon,
                       case
                         when p.activo = 0 then 'INACTIVA'
                         when p.fecha_fin is not null and p.fecha_fin < now() then 'EXPIRADA'
                         when p.fecha_inicio is not null and p.fecha_inicio > now() then 'PROGRAMADA'
                         else 'ACTIVA'
                       end as estado
                  from promocion p
                 order by p.activo desc, p.fecha_fin is null desc, p.fecha_fin asc, p.id desc
                """
        );
        return ApiResponse.ok("Promociones cargadas", rows);
    }

    private Map<String, Object> buildFranja(String nombre, String rango, long pedidos, long total) {
        BigDecimal porcentaje = total > 0
                ? BigDecimal.valueOf(pedidos * 100.0 / total).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("nombre", nombre);
        map.put("rango", rango);
        map.put("pedidos", pedidos);
        map.put("porcentaje", porcentaje);
        return map;
    }

    private BigDecimal queryDecimal(String sql, Object... args) {
        BigDecimal value = jdbcTemplate.queryForObject(sql, BigDecimal.class, args);
        return value != null ? value : BigDecimal.ZERO;
    }

    private long queryLong(String sql, Object... args) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class, args);
        return value != null ? value : 0L;
    }

    private long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number n) return n.longValue();
        return Long.parseLong(String.valueOf(value));
    }

    private int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number n) return n.intValue();
        return Integer.parseInt(String.valueOf(value));
    }
}

