Auth.requireAdmin();

const estadoBadgeClass = {
  1: 'badge-yellow',
  2: 'badge-blue',
  3: 'badge-blue',
  4: 'badge-gold',
  5: 'badge-yellow',
  6: 'badge-green',
  7: 'badge-red',
  8: 'badge-red',
};

function toDate(value) {
  if (!value) return null;
  const d = new Date(value);
  return Number.isNaN(d.getTime()) ? null : d;
}

function isSameDay(a, b) {
  return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate();
}

function money(value) {
  return fmt.money(value || 0);
}

function porcentajeVariacion(actual, previo) {
  if (!previo) return actual > 0 ? 100 : 0;
  return ((actual - previo) / previo) * 100;
}

function deltaClass(value) {
  if (value > 0) return 'stat-delta trend-up';
  if (value < 0) return 'stat-delta trend-down';
  return 'stat-delta trend-flat';
}

function escapeHtml(txt) {
  return String(txt || '').replace(/[&<>"]/g, (ch) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[ch]));
}

async function cargarTodosProductos() {
  let page = 0;
  const size = 60;
  let totalPages = 1;
  const acumulado = [];

  while (page < totalPages) {
    const data = await API.get(`/api/admin/productos?page=${page}&size=${size}`);
    const content = Array.isArray(data.content) ? data.content : [];
    acumulado.push(...content);
    totalPages = Number(data.totalPages || 1);
    page += 1;
  }

  return acumulado;
}

function renderKpis(pedidos, productos) {
  const now = new Date();
  const ayer = new Date(now);
  ayer.setDate(now.getDate() - 1);
  const hace7Dias = new Date(now);
  hace7Dias.setDate(now.getDate() - 7);

  let pedidosHoy = 0;
  let pedidosAyer = 0;
  let ingresosHoy = 0;
  let ingresosAyer = 0;
  let completados7d = 0;

  for (const p of pedidos) {
    const fecha = toDate(p.fechaCreacion);
    if (!fecha) continue;

    if (isSameDay(fecha, now)) {
      pedidosHoy += 1;
      ingresosHoy += Number(p.total || 0);
    } else if (isSameDay(fecha, ayer)) {
      pedidosAyer += 1;
      ingresosAyer += Number(p.total || 0);
    }

    if (fecha >= hace7Dias && p.estadoId === 6) {
      completados7d += 1;
    }
  }

  const stockCritico = productos.filter((x) => !x.hayStock).length;

  const pedidosDelta = pedidosHoy - pedidosAyer;
  const ingresosPct = porcentajeVariacion(ingresosHoy, ingresosAyer);

  const kpiPedidosHoy = document.getElementById('kpi-pedidos-hoy');
  const kpiPedidosHoyDelta = document.getElementById('kpi-pedidos-hoy-delta');
  const kpiIngresosHoy = document.getElementById('kpi-ingresos-hoy');
  const kpiIngresosHoyDelta = document.getElementById('kpi-ingresos-hoy-delta');
  const kpiStockCritico = document.getElementById('kpi-stock-critico');
  const kpiCompletados7d = document.getElementById('kpi-completados-7d');

  kpiPedidosHoy.textContent = String(pedidosHoy);
  kpiPedidosHoyDelta.className = deltaClass(pedidosDelta);
  kpiPedidosHoyDelta.textContent = `${pedidosDelta >= 0 ? '+' : ''}${pedidosDelta} vs ayer`;

  kpiIngresosHoy.textContent = money(ingresosHoy);
  kpiIngresosHoyDelta.className = deltaClass(ingresosPct);
  kpiIngresosHoyDelta.textContent = `${ingresosPct >= 0 ? '+' : ''}${ingresosPct.toFixed(1)}% vs ayer`;

  kpiStockCritico.textContent = String(stockCritico);
  kpiCompletados7d.textContent = String(completados7d);
}

function renderUrgentes(pedidos) {
  const body = document.getElementById('dash-urgentes-body');
  const activos = pedidos
    .filter((p) => ![6, 7, 8].includes(Number(p.estadoId)))
    .sort((a, b) => new Date(b.fechaCreacion) - new Date(a.fechaCreacion))
    .slice(0, 6);

  if (!activos.length) {
    body.innerHTML = '<tr><td colspan="3" class="table-empty-row">No hay pedidos urgentes en este momento.</td></tr>';
    return;
  }

  body.innerHTML = activos.map((p) => {
    const badgeCls = estadoBadgeClass[p.estadoId] || 'badge-ghost';
    const codigo = escapeHtml(p.codigoPedido || `PED-${p.id}`);
    return `
      <tr>
        <td><strong>${codigo}</strong><div class="small text-secondary">${fmt.dt(p.fechaCreacion)}</div></td>
        <td><span class="badge ${badgeCls}">${escapeHtml(p.estado || 'Sin estado')}</span></td>
        <td>${money(p.total)}</td>
      </tr>`;
  }).join('');
}

function renderStockAlertas(productos) {
  const root = document.getElementById('dash-stock-alerts');
  const criticos = productos.filter((p) => !p.hayStock).slice(0, 6);

  if (!criticos.length) {
    root.innerHTML = '<div class="admin-note">No hay alertas de stock crítico.</div>';
    return;
  }

  root.innerHTML = criticos.map((p) => `
    <div class="admin-note d-flex justify-content-between gap-2">
      <div>
        <strong>${escapeHtml(p.nombre)}</strong>
        <div class="small">${escapeHtml(p.nombreCategoria || 'Sin categoría')}</div>
      </div>
      <div class="text-end fw-bold text-danger">Sin stock</div>
    </div>`).join('');
}

async function initDashboard() {
  document.getElementById('dash-urgentes-body').innerHTML = '<tr><td colspan="3" class="text-center py-4"><div class="spinner-border text-danger spinner-border-sm" role="status"></div> Cargando...</td></tr>';
  document.getElementById('dash-stock-alerts').innerHTML = '<div class="text-center py-3"><div class="spinner-border text-danger spinner-border-sm" role="status"></div></div>';
  try {
    const [pedidosRes, productos] = await Promise.all([
      API.get('/api/admin/pedidos'),
      cargarTodosProductos(),
    ]);

    const pedidos = Array.isArray(pedidosRes) ? pedidosRes : [];

    renderKpis(pedidos, productos);
    renderUrgentes(pedidos);
    renderStockAlertas(productos);
  } catch (e) {
    Toast.err(`No se pudo cargar dashboard: ${e.message}`);

    document.getElementById('dash-urgentes-body').innerHTML =
      '<tr><td colspan="3" class="table-error-row">No se pudieron cargar pedidos urgentes.</td></tr>';
    document.getElementById('dash-stock-alerts').innerHTML =
      '<div class="admin-note">No se pudieron cargar alertas de stock.</div>';
  }
}

document.addEventListener('DOMContentLoaded', initDashboard);
