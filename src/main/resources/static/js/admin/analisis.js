Auth.requireAdmin();

function num(v) {
  const n = Number(v);
  return Number.isFinite(n) ? n : 0;
}

function escapeHtml(txt) {
  return String(txt || '').replace(/[&<>\"]/g, (ch) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '\"': '&quot;' }[ch]));
}

async function loadAnalisis() {
  try {
    const data = await API.get('/api/admin/insights/analisis?days=30');

    const top = Array.isArray(data.topProductos) ? data.topProductos : [];
    const horas = Array.isArray(data.horasPico) ? data.horasPico : [];
    const rot = Array.isArray(data.rotacionCategorias) ? data.rotacionCategorias : [];

    const topBody = document.getElementById('an-top-body');
    if (!top.length) {
      topBody.innerHTML = '<tr><td colspan="3" class="table-empty-row">Sin datos de ventas para el periodo.</td></tr>';
    } else {
      topBody.innerHTML = top.map((p, idx) => {
        const trendClass = idx === 0 ? 'trend-up' : (idx >= 4 ? 'trend-flat' : 'trend-up');
        const trendTxt = idx === 0 ? '+Top' : (idx >= 4 ? 'Base' : '+Alto');
        return `
          <tr>
            <td><div class="text-brand-strong fw-semibold">${escapeHtml(p.producto)}</div><div class="inv-meta">${escapeHtml(p.categoria || 'Sin categoría')}</div></td>
            <td>${num(p.unidades_vendidas)}</td>
            <td><span class="${trendClass}"><i class="bi bi-arrow-up"></i> ${trendTxt}</span></td>
          </tr>`;
      }).join('');
    }

    const horasRoot = document.getElementById('an-horas-pico');
    if (!horas.length) {
      horasRoot.innerHTML = '<div class="admin-note">Sin datos de horas pico.</div>';
    } else {
      const maxPct = Math.max(...horas.map((h) => num(h.porcentaje)), 0);
      horasRoot.innerHTML = horas.map((h) => {
        const active = num(h.porcentaje) === maxPct ? ' analysis-peak-active' : '';
        return `
          <article class="analysis-peak-block${active}">
            <div class="analysis-peak-title">${escapeHtml(h.nombre)}</div>
            <div class="analysis-peak-range">${escapeHtml(h.rango)}</div>
            <div class="analysis-peak-value">${num(h.porcentaje).toFixed(1)}%</div>
          </article>`;
      }).join('');
    }

    const rotRoot = document.getElementById('an-rotacion');
    if (!rot.length) {
      rotRoot.innerHTML = '<div class="admin-note">Sin información de rotación por categoría.</div>';
    } else {
      rotRoot.innerHTML = rot.map((r, idx) => {
        const pct = Math.max(0, Math.min(100, num(r.porcentaje)));
        const fillClass = idx === 0 ? 'kpi-fill-gold' : (idx === 1 ? 'kpi-fill-primary' : 'kpi-fill-muted');
        return `
          <div>
            <div class="kpi-pair mb-1"><span>${escapeHtml(r.categoria || 'Sin categoría')}</span><strong>${pct.toFixed(1)}%</strong></div>
            <div class="kpi-track"><div class="${fillClass}" style="width:${pct}%"></div></div>
          </div>`;
      }).join('');
    }
  } catch (e) {
    document.getElementById('an-top-body').innerHTML = '<tr><td colspan="3" class="table-error-row">No se pudo cargar análisis.</td></tr>';
    document.getElementById('an-horas-pico').innerHTML = `<div class="admin-note">Error: ${e.message}</div>`;
    document.getElementById('an-rotacion').innerHTML = '<div class="admin-note">Sin datos disponibles.</div>';
  }
}

document.addEventListener('DOMContentLoaded', loadAnalisis);
