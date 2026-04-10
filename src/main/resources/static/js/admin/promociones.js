/* ═══════════════════════════════════════════════
   ADMIN — Promociones CRUD (RF04)
   ═══════════════════════════════════════════════ */

document.addEventListener('DOMContentLoaded', () => {
  if (!Auth.requireAdmin()) return;
  loadPromos();
});

let allPromos = [];
let editingPromoId = null;

async function loadPromos() {
  try {
    allPromos = await API.get('/api/admin/promociones');
    renderPromos();
  } catch {}
}

function renderPromos() {
  const grid = document.getElementById('promo-grid');
  if (!allPromos || allPromos.length === 0) {
    grid.innerHTML = '<p class="text-secondary">No hay promociones registradas.</p>';
    return;
  }
  grid.innerHTML = allPromos.map(p => {
    const estadoColor = {ACTIVA:'bg-success',INACTIVA:'bg-secondary',EXPIRADA:'bg-danger',PROGRAMADA:'bg-info'}[p.estado]||'bg-secondary';
    const tipoLabel = {PORCENTAJE:`${p.valorDescuento}%`,MONTO_FIJO:`S/${p.valorDescuento}`,'2X1':'2x1',ENVIO_GRATIS:'Envío gratis'}[p.tipoDescuento]||p.tipoDescuento;
    return `
      <div class="card p-3">
        <div class="d-flex justify-content-between align-items-start mb-2">
          <div>
            <strong>${p.nombre}</strong>
            <span class="badge ${estadoColor} ms-2">${p.estado}</span>
          </div>
          <div class="d-flex gap-1">
            <button class="btn btn-sm btn-outline-brand" onclick="editPromo(${p.id})" title="Editar"><i class="bi bi-pencil"></i></button>
            <button class="btn btn-sm ${p.activo ? 'btn-outline-danger' : 'btn-outline-success'}" onclick="togglePromo(${p.id})" title="${p.activo ? 'Desactivar' : 'Activar'}">
              <i class="bi ${p.activo ? 'bi-pause' : 'bi-play'}"></i>
            </button>
          </div>
        </div>
        <div class="d-flex flex-wrap gap-2 small text-secondary">
          <span><i class="bi bi-tag me-1"></i>${tipoLabel}</span>
          ${p.codigoCupon ? `<span><i class="bi bi-key me-1"></i>${p.codigoCupon}</span>` : ''}
          ${p.montoMinimo > 0 ? `<span>Mín: S/${p.montoMinimo}</span>` : ''}
        </div>
        <div class="small text-secondary mt-1">
          ${p.fechaInicio ? fmt.date(p.fechaInicio) : '—'} → ${p.fechaFin ? fmt.date(p.fechaFin) : 'Sin límite'}
        </div>
      </div>
    `;
  }).join('');
}

function openPromoModal(promo = null) {
  editingPromoId = promo?.id || null;
  const f = (id) => document.getElementById(id) || document.querySelector(`[data-field="${id}"]`);
  const form = document.querySelector('#modal-promo form');
  if (!form) return;

  // Rebuild the form to work with API
  const modal = document.querySelector('#modal-promo .modal-body');
  modal.innerHTML = `
    <form id="promo-form" onsubmit="event.preventDefault(); guardarPromo();">
      <div class="field mb-3">
        <label class="label">Nombre de campaña</label>
        <input type="text" class="form-control" id="pf-nombre" required value="${promo?.nombre||''}"/>
      </div>
      <div class="row g-3 mb-3">
        <div class="col-md-6 field">
          <label class="label">Código cupón</label>
          <input type="text" class="form-control text-uppercase" id="pf-cupon" value="${promo?.codigoCupon||''}"/>
        </div>
        <div class="col-md-6 field">
          <label class="label">Tipo de descuento</label>
          <select class="form-select" id="pf-tipo">
            <option value="PORCENTAJE" ${promo?.tipoDescuento==='PORCENTAJE'?'selected':''}>Porcentaje (%)</option>
            <option value="MONTO_FIJO" ${promo?.tipoDescuento==='MONTO_FIJO'?'selected':''}>Monto fijo (S/)</option>
            <option value="2X1" ${promo?.tipoDescuento==='2X1'?'selected':''}>2x1</option>
            <option value="ENVIO_GRATIS" ${promo?.tipoDescuento==='ENVIO_GRATIS'?'selected':''}>Envío gratis</option>
          </select>
        </div>
      </div>
      <div class="row g-3 mb-3">
        <div class="col-md-4 field">
          <label class="label">Valor descuento</label>
          <input type="number" step="0.01" class="form-control" id="pf-valor" value="${promo?.valorDescuento||0}"/>
        </div>
        <div class="col-md-4 field">
          <label class="label">Monto mínimo</label>
          <input type="number" step="0.01" class="form-control" id="pf-minimo" value="${promo?.montoMinimo||0}"/>
        </div>
        <div class="col-md-4 field">
          <label class="label">Aplica a</label>
          <select class="form-select" id="pf-aplica">
            <option value="CARRITO" ${promo?.aplicaA==='CARRITO'?'selected':''}>Carrito</option>
            <option value="PRODUCTO" ${promo?.aplicaA==='PRODUCTO'?'selected':''}>Producto</option>
          </select>
        </div>
      </div>
      <div class="row g-3 mb-3">
        <div class="col-md-6 field">
          <label class="label">Fecha inicio</label>
          <input type="datetime-local" class="form-control" id="pf-inicio" value="${promo?.fechaInicio?.substring(0,16)||''}"/>
        </div>
        <div class="col-md-6 field">
          <label class="label">Fecha fin</label>
          <input type="datetime-local" class="form-control" id="pf-fin" value="${promo?.fechaFin?.substring(0,16)||''}"/>
        </div>
      </div>
      <button type="submit" class="btn btn-brand w-100"><i class="bi bi-check-lg"></i> ${promo ? 'Actualizar' : 'Crear'} promoción</button>
    </form>
  `;
  document.querySelector('#modal-promo .modal-title').textContent = promo ? 'Editar promoción' : 'Nueva promoción';
  Modal.open('modal-promo');
}

function editPromo(id) {
  const promo = allPromos.find(p => p.id === id);
  if (promo) openPromoModal(promo);
}

async function guardarPromo() {
  const body = {
    nombre: document.getElementById('pf-nombre').value,
    tipoDescuento: document.getElementById('pf-tipo').value,
    valorDescuento: parseFloat(document.getElementById('pf-valor').value) || 0,
    montoMinimo: parseFloat(document.getElementById('pf-minimo').value) || 0,
    aplicaA: document.getElementById('pf-aplica').value,
    fechaInicio: document.getElementById('pf-inicio').value || null,
    fechaFin: document.getElementById('pf-fin').value || null,
    codigoCupon: document.getElementById('pf-cupon').value || null,
    activo: true
  };
  try {
    if (editingPromoId) {
      await API.put(`/api/admin/promociones/${editingPromoId}`, body);
      Toast.ok('Promoción actualizada');
    } else {
      await API.post('/api/admin/promociones', body);
      Toast.ok('Promoción creada');
    }
    Modal.close('modal-promo');
    loadPromos();
  } catch {}
}

async function togglePromo(id) {
  try {
    const result = await API.patch(`/api/admin/promociones/${id}/toggle`);
    Toast.ok(result ? 'Promoción activada' : 'Promoción desactivada');
    loadPromos();
  } catch {}
}

// Override the "Nueva promoción" button to use our dynamic form
document.addEventListener('DOMContentLoaded', () => {
  const btn = document.querySelector('.admin-toolbar button');
  if (btn) btn.onclick = () => openPromoModal();
});
