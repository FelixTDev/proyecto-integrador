/* ═══════════════════════════════════════════════
   PERFIL — Mi Cuenta (RF14/RF15/RF17/RF19/RF21)
   ═══════════════════════════════════════════════ */

document.addEventListener('DOMContentLoaded', () => {
  if (!Auth.requireClient()) return;
  loadProfile();
  loadHistorial();
  loadDirecciones();
  loadPuntos();
  loadReclamos();
  loadNotificaciones();

  const hash = window.location.hash?.replace('#tab-','').replace('#','');
  if (hash && document.getElementById('tab-' + hash)) {
    const link = [...document.querySelectorAll('.p-nav-link')].find(l => l.textContent.toLowerCase().includes(hash.substring(0,4)));
    if (link) switchTab(hash, link);
  }
});

/* ─ Tab switching ─ */
function switchTab(tab, el) {
  document.querySelectorAll('.p-content').forEach(c => c.classList.remove('active'));
  document.querySelectorAll('.p-nav-link').forEach(l => l.classList.remove('active'));
  const target = document.getElementById('tab-' + tab);
  if (target) target.classList.add('active');
  if (el) el.classList.add('active');
}

/* ═══ Profile ═══ */
async function loadProfile() {
  try {
    const data = await API.get('/api/auth/me');
    const el = (id) => document.getElementById(id);
    el('p-name').textContent = data.nombre || '—';
    el('p-email').textContent = data.email || '—';
    const initials = (data.nombre || 'CC').split(' ').map(w => w[0]).join('').substring(0,2).toUpperCase();
    el('p-avatar').textContent = initials;
  } catch {}
}

/* ═══ Historial de Pedidos (RF14) ═══ */
async function loadHistorial() {
  const container = document.getElementById('historial-list');
  try {
    const pedidos = await API.get('/api/pedidos/mis-pedidos');
    if (!pedidos || pedidos.length === 0) {
      container.innerHTML = '<p class="text-secondary">No tienes pedidos aún.</p>';
      return;
    }
    container.innerHTML = pedidos.map(p => `
      <div class="card p-3 mb-3">
        <div class="d-flex flex-wrap justify-content-between align-items-start gap-2">
          <div>
            <strong>${p.codigoPedido || '#'+p.id}</strong>
            <span class="badge ${estadoBadge(p.estado)} ms-2">${p.estado || '—'}</span>
          </div>
          <small class="text-secondary">${fmt.dt(p.fechaCreacion)}</small>
        </div>
        <p class="mb-1 mt-2 text-secondary small">${(p.items||[]).map(i => i.nombre).join(', ') || 'Ver detalles'}</p>
        <div class="d-flex flex-wrap gap-2 mt-2">
          <span class="fw-bold">${fmt.money(p.total)}</span>
          ${(p.estado === 'Pendiente' || p.estado === 'En preparacion' || p.estado === 'Pago confirmado') ? `<a href="personalizar.html?pedidoId=${p.id}" class="btn btn-sm btn-outline-brand"><i class="bi bi-magic"></i> Personalizar</a>` : ''}
          <button class="btn btn-sm btn-outline-brand ms-auto" onclick="verComprobante(${p.id})"><i class="bi bi-receipt"></i> Comprobante</button>
          <button class="btn btn-sm btn-brand" onclick="reordenar(${p.id})"><i class="bi bi-arrow-repeat"></i> Reordenar</button>
          <button class="btn btn-sm btn-outline-secondary" onclick="abrirChat(${p.id})"><i class="bi bi-chat-dots"></i> Chat</button>
        </div>
      </div>
    `).join('');
  } catch { container.innerHTML = '<p class="text-danger">Error al cargar historial.</p>'; }
}

function estadoBadge(estado) {
  const map = { 'Pendiente':'bg-warning text-dark','Pago confirmado':'bg-info','En preparacion':'bg-primary',
    'Listo para entrega':'bg-success','En camino':'bg-info','Entregado':'bg-success',
    'Cancelado':'bg-danger','Rechazado':'bg-danger' };
  return map[estado] || 'bg-secondary';
}

async function verComprobante(pedidoId) {
  try {
    const data = await API.get(`/api/pedidos/${pedidoId}/comprobante`);
    let html = `<h4>Comprobante ${data.codigoPedido||'#'+data.pedidoId}</h4>
      <p><strong>Cliente:</strong> ${data.nombreCliente||'—'}<br>
      <strong>Fecha:</strong> ${fmt.dt(data.fechaCreacion)}<br>
      <strong>Estado:</strong> ${data.estadoActual||'—'}</p>
      <table class="table table-sm"><thead><tr><th>Producto</th><th>Cant.</th><th>P.U.</th><th>Sub</th></tr></thead><tbody>`;
    (data.items||[]).forEach(i => {
      html += `<tr><td>${i.nombre}</td><td>${i.cantidad}</td><td>${fmt.money(i.precioUnitario)}</td><td>${fmt.money(i.subtotalLinea)}</td></tr>`;
    });
    html += `</tbody></table>
      <p class="text-end"><strong>Subtotal:</strong> ${fmt.money(data.subtotal)}<br>
      <strong>Envío:</strong> ${fmt.money(data.costoEnvio)}<br>
      <strong class="fs-5">Total: ${fmt.money(data.total)}</strong></p>`;
    const w = window.open('','_blank','width=600,height=700');
    w.document.write(`<html><head><title>Comprobante</title><link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"></head><body class="p-4">${html}<button onclick="window.print()" class="btn btn-primary btn-sm mt-3">Imprimir</button></body></html>`);
  } catch { Toast.err('Error al obtener comprobante'); }
}

async function reordenar(pedidoId) {
  try {
    const result = await API.post(`/api/pedidos/${pedidoId}/reordenar`, {});
    Toast.ok('Productos agregados al carrito (' + (result.itemsAgregados||0) + ')');
  } catch {}
}

function abrirChat(pedidoId) {
  window.location.href = `/pages/cliente/chat.html?pedidoId=${pedidoId}`;
}

/* ═══ Direcciones (RF17) ═══ */
let zonas = [];
async function loadDirecciones() {
  try {
    zonas = await API.get('/api/cliente/direcciones/zonas');
  } catch { zonas = []; }
  renderDirecciones();
}

async function renderDirecciones() {
  const container = document.getElementById('dir-list');
  try {
    const dirs = await API.get('/api/cliente/direcciones');
    if (!dirs || dirs.length === 0) {
      container.innerHTML = '<p class="text-secondary">No tienes direcciones registradas.</p>';
      return;
    }
    container.innerHTML = dirs.map(d => `
      <div class="card p-3 mb-2 ${d.esPrincipal ? 'border-warning' : ''}">
        <div class="d-flex justify-content-between align-items-start">
          <div>
            <strong>${d.etiqueta || 'Sin etiqueta'}</strong>
            ${d.esPrincipal ? '<span class="badge bg-warning text-dark ms-2">Principal</span>' : ''}
            ${!d.activo ? '<span class="badge bg-secondary ms-2">Inactiva</span>' : ''}
          </div>
          <div class="d-flex gap-1">
            ${d.activo && !d.esPrincipal ? `<button class="btn btn-sm btn-outline-warning" onclick="marcarPrincipal(${d.id})" title="Hacer principal"><i class="bi bi-star"></i></button>` : ''}
            <button class="btn btn-sm btn-outline-brand" onclick="editarDireccion(${d.id})" title="Editar"><i class="bi bi-pencil"></i></button>
            ${d.activo ? `<button class="btn btn-sm btn-outline-danger" onclick="desactivarDireccion(${d.id})" title="Eliminar"><i class="bi bi-trash"></i></button>` : ''}
          </div>
        </div>
        <p class="text-secondary small mb-0 mt-1">${d.direccionCompleta} ${d.zonaNombre ? '('+d.zonaNombre+')' : ''}</p>
      </div>
    `).join('');
  } catch { container.innerHTML = '<p class="text-danger">Error al cargar direcciones.</p>'; }
}

function openDireccionModal(dir = null) {
  document.getElementById('dir-modal-title').textContent = dir ? 'Editar dirección' : 'Nueva dirección';
  document.getElementById('dir-id').value = dir?.id || '';
  document.getElementById('dir-etiqueta').value = dir?.etiqueta || '';
  document.getElementById('dir-direccion').value = dir?.direccionCompleta || '';
  document.getElementById('dir-ref').value = dir?.referencia || '';
  document.getElementById('dir-dest').value = dir?.destinatarioNombre || '';
  document.getElementById('dir-tel').value = dir?.destinatarioTelefono || '';
  document.getElementById('dir-principal').checked = dir?.esPrincipal || false;

  const sel = document.getElementById('dir-zona');
  sel.innerHTML = '<option value="">Sin zona</option>' + zonas.map(z =>
    `<option value="${z.id}" ${z.id === dir?.zonaId ? 'selected' : ''}>${z.nombreDistrito} - ${fmt.money(z.costoDelivery)}</option>`
  ).join('');
  Modal.open('modal-dir');
}

async function editarDireccion(dirId) {
  try {
    const dirs = await API.get('/api/cliente/direcciones');
    const dir = dirs.find(d => d.id === dirId);
    if (dir) openDireccionModal(dir);
  } catch {}
}

async function guardarDireccion() {
  const id = document.getElementById('dir-id').value;
  const body = {
    etiqueta: document.getElementById('dir-etiqueta').value,
    direccionCompleta: document.getElementById('dir-direccion').value,
    referencia: document.getElementById('dir-ref').value,
    destinatarioNombre: document.getElementById('dir-dest').value,
    destinatarioTelefono: document.getElementById('dir-tel').value,
    zonaId: document.getElementById('dir-zona').value || null,
    esPrincipal: document.getElementById('dir-principal').checked
  };
  try {
    if (id) {
      await API.put(`/api/cliente/direcciones/${id}`, body);
    } else {
      await API.post('/api/cliente/direcciones', body);
    }
    Modal.close('modal-dir');
    Toast.ok('Dirección guardada');
    renderDirecciones();
  } catch {}
}

async function desactivarDireccion(dirId) {
  if (!confirm('¿Eliminar esta dirección?')) return;
  try {
    await API.delete(`/api/cliente/direcciones/${dirId}`);
    Toast.ok('Dirección eliminada');
    renderDirecciones();
  } catch {}
}

async function marcarPrincipal(dirId) {
  try {
    await API.patch(`/api/cliente/direcciones/${dirId}/principal`);
    Toast.ok('Dirección marcada como principal');
    renderDirecciones();
  } catch {}
}

/* ═══ Puntos de Fidelidad (RF19) ═══ */
async function loadPuntos() {
  try {
    const data = await API.get('/api/cliente/puntos');
    document.getElementById('puntos-saldo').textContent = data.saldo || 0;
    document.getElementById('p-puntos').innerHTML = `<i class="bi bi-star-fill me-1"></i>${data.saldo || 0} Puntos`;

    const container = document.getElementById('puntos-list');
    if (!data.movimientos || data.movimientos.length === 0) {
      container.innerHTML = '<p class="text-secondary">Sin movimientos aún.</p>';
      return;
    }
    container.innerHTML = data.movimientos.map(m => `
      <div class="card p-3 mb-2">
        <div class="d-flex justify-content-between">
          <div>
            <span class="badge ${m.tipo === 'GANADO' ? 'bg-success' : m.tipo === 'CANJEADO' ? 'bg-warning text-dark' : 'bg-secondary'}">${m.tipo}</span>
            <span class="ms-2">${m.descripcion || '—'}</span>
          </div>
          <div class="text-end">
            <strong class="${m.puntos > 0 ? 'text-success' : 'text-danger'}">${m.puntos > 0 ? '+' : ''}${m.puntos}</strong>
            <small class="d-block text-secondary">${fmt.dt(m.fecha)}</small>
          </div>
        </div>
      </div>
    `).join('');
  } catch {
    document.getElementById('puntos-saldo').textContent = '—';
    document.getElementById('puntos-list').innerHTML = '<p class="text-secondary">No se pudo cargar los puntos.</p>';
  }
}

/* ═══ Reclamos (RF15) ═══ */
function toggleReclamoForm() {
  const card = document.getElementById('reclamo-form-card');
  card.style.display = card.style.display === 'none' ? 'block' : 'none';
  if (card.style.display === 'block') loadPedidosForReclamo();
}

async function loadPedidosForReclamo() {
  try {
    const pedidos = await API.get('/api/pedidos/mis-pedidos');
    const sel = document.getElementById('reclamo-pedido');
    sel.innerHTML = pedidos.map(p => `<option value="${p.id}">${p.codigoPedido || '#'+p.id} — ${fmt.dt(p.fechaCreacion)}</option>`).join('');
  } catch {}
}

async function enviarReclamo() {
  const body = {
    pedidoId: parseInt(document.getElementById('reclamo-pedido').value),
    tipo: document.getElementById('reclamo-tipo').value,
    descripcion: document.getElementById('reclamo-desc').value
  };
  try {
    await API.post('/api/cliente/reclamos', body);
    Toast.ok('Reclamo registrado exitosamente');
    document.getElementById('reclamo-form').reset();
    document.getElementById('reclamo-form-card').style.display = 'none';
    loadReclamos();
  } catch {}
}

async function loadReclamos() {
  const container = document.getElementById('reclamos-list');
  try {
    const reclamos = await API.get('/api/cliente/reclamos');
    if (!reclamos || reclamos.length === 0) {
      container.innerHTML = '<p class="text-secondary">No tienes reclamos registrados.</p>';
      return;
    }
    container.innerHTML = reclamos.map(r => `
      <div class="card p-3 mb-2">
        <div class="d-flex justify-content-between align-items-start">
          <div>
            <strong>Reclamo #${r.id}</strong>
            <span class="badge ${reclamoBadge(r.estado)} ms-2">${r.estado}</span>
            <span class="badge bg-secondary ms-1">${r.tipo}</span>
          </div>
          <small class="text-secondary">${fmt.dt(r.fechaCreacion)}</small>
        </div>
        <p class="text-secondary small mb-1 mt-2">${r.descripcion}</p>
        ${r.detalleResolucion ? `<p class="small mb-0"><strong>Resolución:</strong> ${r.detalleResolucion}</p>` : ''}
        ${r.montoReembolso > 0 ? `<p class="small text-success mb-0"><strong>Reembolso:</strong> ${fmt.money(r.montoReembolso)}</p>` : ''}
      </div>
    `).join('');
  } catch { container.innerHTML = '<p class="text-danger">Error al cargar reclamos.</p>'; }
}

function reclamoBadge(estado) {
  return { ABIERTO:'bg-warning text-dark', EN_REVISION:'bg-info', RESUELTO:'bg-success', CERRADO:'bg-secondary' }[estado] || 'bg-secondary';
}

/* ═══ Notificaciones (RF21) ═══ */
async function loadNotificaciones() {
  const container = document.getElementById('notif-list');
  try {
    const notifs = await API.get('/api/cliente/notificaciones');
    if (!notifs || notifs.length === 0) {
      container.innerHTML = '<p class="text-secondary">Sin notificaciones.</p>';
      return;
    }
    container.innerHTML = notifs.map(n => `
      <div class="card p-3 mb-2 ${n.leida ? 'opacity-75' : ''}" style="cursor:pointer" onclick="marcarNotifLeida(${n.id}, this)">
        <div class="d-flex justify-content-between align-items-start">
          <div>
            ${!n.leida ? '<i class="bi bi-circle-fill text-warning me-2" style="font-size:0.5rem"></i>' : ''}
            <strong>${n.asunto || 'Notificación'}</strong>
          </div>
          <small class="text-secondary">${fmt.dt(n.fechaEnvio)}</small>
        </div>
        <p class="text-secondary small mb-0 mt-1">${n.mensaje}</p>
      </div>
    `).join('');
  } catch { container.innerHTML = '<p class="text-danger">Error al cargar notificaciones.</p>'; }
}

async function marcarNotifLeida(notifId, el) {
  try {
    await API.patch(`/api/cliente/notificaciones/${notifId}/leida`);
    el.classList.add('opacity-75');
    const dot = el.querySelector('.bi-circle-fill');
    if (dot) dot.remove();
  } catch {}
}
