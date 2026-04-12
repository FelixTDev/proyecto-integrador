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
import pe.edu.utp.proyecto_integrador_casachantilly.admin.dto.HoraPicoDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.admin.dto.InsightAnalisisResponseDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.admin.dto.InsightKpiDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.admin.dto.InsightReporteResponseDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.admin.dto.MetodoPagoInsightDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.admin.dto.PromocionInsightDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.admin.dto.RotacionCategoriaDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.admin.dto.TipoEntregaDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.admin.dto.TopProductoDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.admin.dto.VentaDiariaDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.dto.ApiResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Tag(name = "Admin - Insights", description = "Indicadores y analitica para dashboard/reportes/promociones")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin/insights")
@PreAuthorize("hasRole('ADMIN')")
public class AdminInsightsController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Operation(summary = "KPIs y series para reportes comerciales")
    @GetMapping("/reportes")
    public ApiResponse<InsightReporteResponseDTO> reportes(@RequestParam(defaultValue = "14") int days) {
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
        BigDecimal cancelPct = pedidosTotales > 0
                ? BigDecimal.valueOf(cancelados * 100.0 / pedidosTotales).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<VentaDiariaDTO> ventasDiarias = jdbcTemplate.queryForList(
                """
                select fecha, total_pedidos, coalesce(ingresos_brutos,0) as ingresos_brutos
                  from vw_ventas_diarias
                 where fecha >= ?
                 order by fecha asc
                """,
                java.sql.Date.valueOf(desde)
        ).stream().map(this::toVentaDiaria).toList();

        long delivery = queryLong(
                "select count(*) from pedido p where p.fecha_creacion >= ? and coalesce(p.es_recojo_tienda,0)=0",
                java.sql.Date.valueOf(desde)
        );
        long recojo = queryLong(
                "select count(*) from pedido p where p.fecha_creacion >= ? and coalesce(p.es_recojo_tienda,0)=1",
                java.sql.Date.valueOf(desde)
        );
        long totalEntrega = delivery + recojo;

        TipoEntregaDTO tiposEntrega = new TipoEntregaDTO(
                totalEntrega > 0 ? BigDecimal.valueOf(delivery * 100.0 / totalEntrega).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO,
                totalEntrega > 0 ? BigDecimal.valueOf(recojo * 100.0 / totalEntrega).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO,
                delivery,
                recojo
        );

        List<MetodoPagoInsightDTO> metodos = jdbcTemplate.queryForList(
                """
                select metodo, total_transacciones, coalesce(monto_total,0) as monto_total, coalesce(porcentaje_uso,0) as porcentaje_uso
                  from vw_metodos_pago_stats
                 order by total_transacciones desc
                """
        ).stream().map(this::toMetodoPagoDto).toList();

        InsightKpiDTO kpis = new InsightKpiDTO(
                ventasBrutas,
                pedidosCompletados,
                ticketPromedio,
                cancelPct,
                pedidosTotales
        );

        InsightReporteResponseDTO data = new InsightReporteResponseDTO(kpis, ventasDiarias, tiposEntrega, metodos, rango);
        return ApiResponse.ok("Reportes generados", data);
    }

    @Operation(summary = "Analisis de productos y demanda horaria")
    @GetMapping("/analisis")
    public ApiResponse<InsightAnalisisResponseDTO> analisis(@RequestParam(defaultValue = "30") int days) {
        int rango = Math.max(1, Math.min(days, 365));

        List<TopProductoDTO> topProductos = jdbcTemplate.queryForList(
                """
                select producto, categoria, coalesce(unidades_vendidas,0) as unidades_vendidas, ranking_unidades
                  from vw_productos_top
                 order by ranking_unidades asc
                 limit 5
                """
        ).stream().map(this::toTopProducto).toList();

        List<Map<String, Object>> horasRaw = jdbcTemplate.queryForList(
                """
                select hora, num_pedidos
                  from vw_horas_pico
                 order by num_pedidos desc, hora asc
                """
        );

        long totalPedidosHora = horasRaw.stream().mapToLong(m -> toLong(m.get("num_pedidos"))).sum();
        long manana = 0L;
        long tarde = 0L;
        long noche = 0L;
        for (Map<String, Object> row : horasRaw) {
            int h = toInt(row.get("hora"));
            long n = toLong(row.get("num_pedidos"));
            if (h >= 9 && h <= 12) manana += n;
            else if (h >= 13 && h <= 17) tarde += n;
            else if (h >= 18 && h <= 21) noche += n;
        }

        List<HoraPicoDTO> horasPico = new ArrayList<>();
        horasPico.add(buildFranja("Manana", "9:00am - 12:00pm", manana, totalPedidosHora));
        horasPico.add(buildFranja("Tarde (Pico)", "1:00pm - 5:00pm", tarde, totalPedidosHora));
        horasPico.add(buildFranja("Noche", "6:00pm - 9:00pm", noche, totalPedidosHora));

        List<Map<String, Object>> rotacionRaw = jdbcTemplate.queryForList(
                """
                select categoria, coalesce(sum(unidades_vendidas),0) as unidades
                  from vw_productos_top
                 group by categoria
                 order by unidades desc
                """
        );
        long totalUnidades = rotacionRaw.stream().mapToLong(m -> toLong(m.get("unidades"))).sum();
        List<RotacionCategoriaDTO> rotacionCategorias = rotacionRaw.stream()
                .map(row -> {
                    long unidades = toLong(row.get("unidades"));
                    BigDecimal pct = totalUnidades > 0
                            ? BigDecimal.valueOf(unidades * 100.0 / totalUnidades).setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return new RotacionCategoriaDTO(String.valueOf(row.get("categoria")), unidades, pct);
                })
                .toList();

        InsightAnalisisResponseDTO data = new InsightAnalisisResponseDTO(topProductos, horasPico, rotacionCategorias, rango);
        return ApiResponse.ok("Analisis generado", data);
    }

    @Operation(summary = "Listado de promociones registradas")
    @GetMapping("/promociones")
    public ApiResponse<List<PromocionInsightDTO>> promociones() {
        List<PromocionInsightDTO> rows = jdbcTemplate.queryForList(
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
        ).stream().map(this::toPromocionDto).toList();
        return ApiResponse.ok("Promociones cargadas", rows);
    }

    private HoraPicoDTO buildFranja(String nombre, String rango, long pedidos, long total) {
        BigDecimal porcentaje = total > 0
                ? BigDecimal.valueOf(pedidos * 100.0 / total).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        return new HoraPicoDTO(nombre, rango, pedidos, porcentaje);
    }

    private VentaDiariaDTO toVentaDiaria(Map<String, Object> row) {
        Object fechaObj = row.get("fecha");
        LocalDate fecha;
        if (fechaObj instanceof java.sql.Date date) {
            fecha = date.toLocalDate();
        } else {
            fecha = LocalDate.parse(String.valueOf(fechaObj));
        }
        return new VentaDiariaDTO(fecha, toLong(row.get("total_pedidos")), toBigDecimal(row.get("ingresos_brutos")));
    }

    private MetodoPagoInsightDTO toMetodoPagoDto(Map<String, Object> row) {
        return new MetodoPagoInsightDTO(
                String.valueOf(row.get("metodo")),
                toLong(row.get("total_transacciones")),
                toBigDecimal(row.get("monto_total")),
                toBigDecimal(row.get("porcentaje_uso"))
        );
    }

    private TopProductoDTO toTopProducto(Map<String, Object> row) {
        return new TopProductoDTO(
                String.valueOf(row.get("producto")),
                String.valueOf(row.get("categoria")),
                toLong(row.get("unidades_vendidas")),
                toInt(row.get("ranking_unidades"))
        );
    }

    private PromocionInsightDTO toPromocionDto(Map<String, Object> row) {
        return new PromocionInsightDTO(
                toInt(row.get("id")),
                String.valueOf(row.get("nombre")),
                String.valueOf(row.get("tipo_descuento")),
                toBigDecimal(row.get("valor_descuento")),
                toBigDecimal(row.get("monto_minimo")),
                String.valueOf(row.get("aplica_a")),
                toLocalDateTime(row.get("fecha_inicio")),
                toLocalDateTime(row.get("fecha_fin")),
                toInt(row.get("activo")) == 1,
                row.get("codigo_cupon") == null ? null : String.valueOf(row.get("codigo_cupon")),
                String.valueOf(row.get("estado"))
        );
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

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue()).setScale(2, RoundingMode.HALF_UP);
        return new BigDecimal(String.valueOf(value)).setScale(2, RoundingMode.HALF_UP);
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime dt) return dt;
        if (value instanceof Timestamp ts) return ts.toLocalDateTime();
        return LocalDateTime.parse(String.valueOf(value));
    }
}