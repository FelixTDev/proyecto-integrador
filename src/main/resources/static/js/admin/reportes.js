Auth.requireAdmin();

function toNum(v) {
  const n = Number(v);
  return Number.isFinite(n) ? n : 0;
}

function pct(v) {
  return `${toNum(v).toFixed(1)}%`;
}

function fechaCorta(v) {
  if (!v) return '';
  const d = new Date(v);
  if (Number.isNaN(d.getTime())) return String(v);
  return d.toLocaleDateString('es-PE', { day: '2-digit', month: 'short' });
}

function badgeClaseByIndex(i) {
  if (i === 0) return 'badge-red';
  if (i === 1) return 'badge-yellow';
  return 'badge-ghost';
}

async function loadReportes() {
  const rangoEl = document.getElementById('rep-rango');
  const note = document.getElementById('rep-note');

  const label = (rangoEl.value || 'Últimos 14 días').toLowerCase();
  let days = 14;
  if (label.includes('30')) days = 30;
  if (label.includes('60')) days = 60;

  try {
    note.innerHTML = '<i class="bi bi-arrow-repeat spin"></i> Cargando indicadores desde BD...';

    const data = await API.get(`/api/admin/insights/reportes?days=${days}`);
    const kpis = data.kpis || {};
    const ventas = Array.isArray(data.ventasDiarias) ? data.ventasDiarias : [];
    const tiposEntrega = data.tiposEntrega || {};
    const metodos = Array.isArray(data.metodosPago) ? data.metodosPago : [];

    document.getElementById('rep-kpi-ventas').textContent = fmt.money(kpis.ventasBrutas || 0);
    document.getElementById('rep-kpi-completados').textContent = String(kpis.pedidosCompletados || 0);
    document.getElementById('rep-kpi-ticket').textContent = fmt.money(kpis.ticketPromedio || 0);
    document.getElementById('rep-kpi-cancel').textContent = `${toNum(kpis.cancelacionesPct).toFixed(2)}%`;

    document.getElementById('rep-kpi-ventas-sub').textContent = `Rango actual (${days} días)`;
    document.getElementById('rep-kpi-completados-sub').textContent = `${kpis.pedidosTotales || 0} pedidos totales`;

    const barsRoot = document.getElementById('rep-bars');
    if (!ventas.length) {
      barsRoot.innerHTML = '<div class="admin-note w-100 text-center">Sin ventas en el rango seleccionado.</div>';
    } else {
      const maxIngreso = Math.max(...ventas.map((x) => toNum(x.ingresos_brutos)), 1);
      barsRoot.innerHTML = ventas.map((x) => {
        const h = Math.max(8, Math.round((toNum(x.ingresos_brutos) / maxIngreso) * 100));
        return `<div class="bar" style="height:${h}%"><div class="bar-label">${fechaCorta(x.fecha)}</div></div>`;
      }).join('');
    }

    const deliveryPct = toNum(tiposEntrega.deliveryPct);
    const recojoPct = toNum(tiposEntrega.recojoPct);
    document.getElementById('rep-delivery-pct').textContent = pct(deliveryPct);
    document.getElementById('rep-recojo-pct').textContent = pct(recojoPct);
    document.getElementById('rep-delivery-fill').style.width = `${Math.max(0, Math.min(100, deliveryPct))}%`;
    document.getElementById('rep-recojo-fill').style.width = `${Math.max(0, Math.min(100, recojoPct))}%`;

    const metodosRoot = document.getElementById('rep-metodos');
    if (!metodos.length) {
      metodosRoot.innerHTML = '<li class="admin-note">No hay métodos de pago registrados.</li>';
    } else {
      metodosRoot.innerHTML = metodos.slice(0, 5).map((m, i) => {
        const p = toNum(m.porcentaje_uso);
        return `<li class="d-flex align-items-center gap-2"><span class="badge ${badgeClaseByIndex(i)}">${p.toFixed(1)}%</span> ${m.metodo}</li>`;
      }).join('');
    }

    note.innerHTML = `<i class="bi bi-check-circle"></i> Datos reales cargados para ${days} días.`;
  } catch (e) {
    note.innerHTML = `<i class="bi bi-exclamation-triangle"></i> Error cargando reportes: ${e.message}`;
  }
}

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('rep-rango')?.addEventListener('change', loadReportes);
  document.getElementById('rep-refresh')?.addEventListener('click', loadReportes);
  loadReportes();
});
