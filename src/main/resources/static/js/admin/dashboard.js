Auth.requireAdmin();

const estadoBadgeClass = {
  1: 'badge-yellow',
  2: 'badge-blue',
  3: 'badge-blue',
  4: 'badge-gold',
  5: 'badge-yellow',
  6: 'badge-green',
  7: 'badge-red',
  8: 'badge-red'
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
  return String(txt || '').replace(/[&<>"']/g, (ch) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[ch]));
}

async function cargarTodosProductos() {
  let page = 0;
  const size = 60;
  let totalPages = 1;
  const acumulado = [];

  while (page < totalPages) {
    const data = await API.get('/api/admin/productos?page=' + page + '&size=' + size);
    const content = Array.isArray(data.content) ? data.content : [];
    acumulado.push(...content);
    totalPages = Number(data.totalPages || 1);
    page += 1;
  }

  return acumulado;
}

function renderKpis(pedidos, productos, chatNoLeidos) {
  const now = new Date();
  const ayer = new Date(now);
  ayer.setDate(now.getDate() - 1);

  let pedidosHoy = 0;
  let pedidosAyer = 0;
  let ingresosHoy = 0;
  let ingresosAyer = 0;

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
  }

  const stockCritico = productos.filter((x) => !x.hayStock).length;
  const pedidosDelta = pedidosHoy - pedidosAyer;
  const ingresosPct = porcentajeVariacion(ingresosHoy, ingresosAyer);

  document.getElementById('kpi-pedidos-hoy').textContent = String(pedidosHoy);
  document.getElementById('kpi-pedidos-hoy-delta').className = deltaClass(pedidosDelta);
  document.getElementById('kpi-pedidos-hoy-delta').textContent = `${pedidosDelta >= 0 ? '+' : ''}${pedidosDelta} vs ayer`;

  document.getElementById('kpi-ingresos-hoy').textContent = money(ingresosHoy);
  document.getElementById('kpi-ingresos-hoy-delta').className = deltaClass(ingresosPct);
  document.getElementById('kpi-ingresos-hoy-delta').textContent = `${ingresosPct >= 0 ? '+' : ''}${ingresosPct.toFixed(1)}% vs ayer`;

  document.getElementById('kpi-stock-critico').textContent = String(stockCritico);
  document.getElementById('kpi-chat-no-leidos').textContent = String(chatNoLeidos || 0);
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
    root.innerHTML = '<div class="admin-note">No hay alertas de stock critico.</div>';
    return;
  }

  root.innerHTML = criticos.map((p) => `
    <div class="admin-note d-flex justify-content-between gap-2">
      <div>
        <strong>${escapeHtml(p.nombre)}</strong>
        <div class="small">${escapeHtml(p.nombreCategoria || 'Sin categoria')}</div>
      </div>
      <div class="text-end fw-bold text-danger">Sin stock</div>
    </div>`).join('');
}

async function renderPickupAlertas(pedidos) {
  const root = document.getElementById('dash-pickup-alerts');
  const activos = pedidos
    .filter((p) => ![6, 7, 8].includes(Number(p.estadoId)))
    .sort((a, b) => new Date(b.fechaCreacion) - new Date(a.fechaCreacion))
    .slice(0, 8);

  if (!activos.length) {
    root.innerHTML = '<div class="admin-note">No hay pedidos activos para evaluar recogida.</div>';
    return;
  }

  const comprobantes = await Promise.all(activos.map((p) => API.get('/api/pedidos/' + p.id + '/comprobante').catch(() => null)));
  const pickups = comprobantes.filter((c) => c && c.recojoEnTienda === true).slice(0, 5);

  if (!pickups.length) {
    root.innerHTML = '<div class="admin-note">No hay recogidas proximas en pedidos activos.</div>';
    return;
  }

  root.innerHTML = pickups.map((c) => `
    <div class="admin-note d-flex justify-content-between gap-2">
      <div>
        <strong>${escapeHtml(c.codigoPedido || '#' + c.pedidoId)}</strong>
        <div class="small">${fmt.dt(c.fechaCreacion)}</div>
      </div>
      <div class="text-end fw-semibold">Recojo en tienda</div>
    </div>`).join('');
}

async function initDashboard() {
  document.getElementById('dash-urgentes-body').innerHTML = '<tr><td colspan="3" class="text-center py-4"><div class="spinner-border text-danger spinner-border-sm" role="status"></div> Cargando...</td></tr>';
  document.getElementById('dash-stock-alerts').innerHTML = '<div class="text-center py-3"><div class="spinner-border text-danger spinner-border-sm" role="status"></div></div>';
  document.getElementById('dash-pickup-alerts').innerHTML = '<div class="text-center py-3"><div class="spinner-border text-danger spinner-border-sm" role="status"></div></div>';

  try {
    const [pedidosRes, productos, chat] = await Promise.all([
      API.get('/api/admin/pedidos'),
      cargarTodosProductos(),
      API.get('/api/chat/no-leidos').catch(() => ({ noLeidos: 0 }))
    ]);

    const pedidos = Array.isArray(pedidosRes) ? pedidosRes : [];
    const noLeidos = Number(chat.noLeidos || 0);

    renderKpis(pedidos, productos, noLeidos);
    renderUrgentes(pedidos);
    renderStockAlertas(productos);
    await renderPickupAlertas(pedidos);
  } catch (e) {
    Toast.err(`No se pudo cargar dashboard: ${e.message}`);
    document.getElementById('dash-urgentes-body').innerHTML = '<tr><td colspan="3" class="table-error-row">No se pudieron cargar pedidos urgentes.</td></tr>';
    document.getElementById('dash-stock-alerts').innerHTML = '<div class="admin-note">No se pudieron cargar alertas de stock.</div>';
    document.getElementById('dash-pickup-alerts').innerHTML = '<div class="admin-note">No se pudieron cargar alertas de recogida.</div>';
  }
}

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('dash-refresh')?.addEventListener('click', initDashboard);
  initDashboard();
});
