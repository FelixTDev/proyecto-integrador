Auth.requireAdmin();

function escapeHtml(txt) {
  return String(txt || '').replace(/[&<>\"]/g, (ch) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '\"': '&quot;' }[ch]));
}

function estadoBadge(estado) {
  const e = String(estado || '').toUpperCase();
  if (e === 'ACTIVA') return 'badge-green';
  if (e === 'EXPIRADA') return 'badge-red';
  if (e === 'PROGRAMADA') return 'badge-yellow';
  return 'badge-ghost';
}

function fmtValor(row) {
  const tipo = String(row.tipo_descuento || '');
  const valor = Number(row.valor_descuento || 0);
  if (tipo === 'PORCENTAJE') return `${valor.toFixed(0)}%`;
  if (tipo === 'MONTO_FIJO') return fmt.money(valor);
  if (tipo === 'ENVIO_GRATIS') return 'Envío gratis';
  return `${tipo} ${valor}`.trim();
}

function fmtFecha(v) {
  if (!v) return 'Sin fecha';
  return fmt.dt(v);
}

async function loadPromos() {
  const root = document.getElementById('promo-grid');
  try {
    const rows = await API.get('/api/admin/insights/promociones');
    const promos = Array.isArray(rows) ? rows : [];

    if (!promos.length) {
      root.innerHTML = '<div class="admin-note">No hay promociones registradas en la base de datos.</div>';
      return;
    }

    root.innerHTML = promos.map((p) => {
      const estado = String(p.estado || 'INACTIVA').toUpperCase();
      const muted = estado !== 'ACTIVA' ? ' promo-muted' : '';
      const fechaFin = p.fecha_fin ? `Válida hasta: ${fmtFecha(p.fecha_fin)}` : 'Sin fecha fin';

      return `
        <article class="promo-card${muted}">
          <span class="badge ${estadoBadge(estado)} promo-status">${escapeHtml(estado)}</span>
          <h2 class="promo-title">${escapeHtml(p.nombre)}</h2>
          <p class="promo-desc">Tipo: ${escapeHtml(p.tipo_descuento)} · Aplica a: ${escapeHtml(p.aplica_a || 'N/A')}</p>
          <span class="promo-code ${p.codigo_cupon ? '' : 'promo-code-muted'}">${escapeHtml(p.codigo_cupon || 'SIN-CUPON')}</span>
          <div class="promo-footer">
            <span class="promo-meta"><i class="bi bi-clock"></i> ${escapeHtml(fechaFin)}</span>
            <div class="promo-actions">
              <span class="badge badge-ghost">Valor: ${escapeHtml(fmtValor(p))}</span>
            </div>
          </div>
        </article>`;
    }).join('');
  } catch (e) {
    root.innerHTML = `<div class="admin-note">No se pudieron cargar promociones: ${e.message}</div>`;
  }
}

document.addEventListener('DOMContentLoaded', loadPromos);
