Auth.requireClient();

let direcciones = [];
let zonas = [];
let historialPedidos = [];

function badgeEstado(estadoId, estadoNombre) {
  const clsMap = {
    1: 'badge-yellow',
    2: 'badge-blue',
    3: 'badge-blue',
    4: 'badge-gold',
    5: 'badge-yellow',
    6: 'badge-green',
    7: 'badge-red',
    8: 'badge-red',
  };
  const cls = clsMap[estadoId] || 'badge-ghost';
  return `<span class="badge ${cls} mb-2">${estadoNombre || 'Sin estado'}</span>`;
}

function switchTab(id, el) {
  document.querySelectorAll('.p-content').forEach((x) => x.classList.remove('active'));
  document.getElementById('tab-' + id).classList.add('active');
  document.querySelectorAll('.p-nav-link').forEach((x) => x.classList.remove('active'));
  el.classList.add('active');
}

async function initPerfil() {
  document.getElementById('p-name').textContent = Auth.email().split('@')[0] || 'Cliente';
  document.getElementById('p-email').textContent = Auth.email();

  await Promise.all([
    cargarZonas(),
    cargarDirecciones(),
    cargarHistorial(),
    cargarNotificaciones(),
  ]);
}

async function cargarZonas() {
  try {
    zonas = await API.get('/api/cliente/zonas-envio');
  } catch {
    zonas = [];
  }
  const sel = document.getElementById('dir-zona');
  if (!sel) return;
  sel.innerHTML = '<option value="">Sin zona específica</option>' +
    (zonas || []).map((z) => `<option value="${z.id}">${z.nombreDistrito} (${fmt.money(z.costoDelivery)})</option>`).join('');
}

async function cargarDirecciones() {
  const list = document.getElementById('dir-list');
  if (!list) return;

  try {
    direcciones = await API.get('/api/cliente/direcciones');
  } catch (e) {
    list.innerHTML = `<div class="card panel-error">No se pudieron cargar direcciones: ${e.message}</div>`;
    return;
  }

  if (!direcciones.length) {
    list.innerHTML = '<div class="card panel-empty">No tienes direcciones registradas.</div>';
    return;
  }

  list.innerHTML = direcciones.map((d) => `
    <div class="dir-card ${d.esPrincipal ? 'main' : ''}">
      ${d.esPrincipal ? '<div class="badge badge-gold dir-badge">Principal</div>' : ''}
      <h4 class="dir-title">${d.etiqueta || 'Dirección'}</h4>
      <p class="dir-line">${d.direccionCompleta}${d.zonaNombre ? `<br>${d.zonaNombre}` : ''}</p>
      <p class="dir-meta">${d.destinatarioNombre || ''} ${d.destinatarioTelefono || ''}</p>
      <div class="action-row">
        <button class="btn btn-soft btn-sm" onclick="editarDireccion(${d.id})">Editar</button>
        ${d.esPrincipal ? '' : `<button class="btn btn-outline-brand btn-sm" onclick="marcarPrincipal(${d.id})">Marcar principal</button>`}
        <button class="btn btn-soft btn-sm btn-soft-danger" onclick="desactivarDireccion(${d.id})">Desactivar</button>
      </div>
    </div>
  `).join('');
}

function openDireccionModal() {
  document.getElementById('dir-modal-title').textContent = 'Nueva dirección';
  document.getElementById('dir-form').reset();
  document.getElementById('dir-id').value = '';
  Modal.open('modal-dir');
}

function editarDireccion(id) {
  const d = direcciones.find((x) => x.id === id);
  if (!d) return;
  document.getElementById('dir-modal-title').textContent = 'Editar dirección';
  document.getElementById('dir-id').value = String(d.id);
  document.getElementById('dir-etiqueta').value = d.etiqueta || '';
  document.getElementById('dir-zona').value = d.zonaId || '';
  document.getElementById('dir-direccion').value = d.direccionCompleta || '';
  document.getElementById('dir-ref').value = d.referencia || '';
  document.getElementById('dir-dest').value = d.destinatarioNombre || '';
  document.getElementById('dir-tel').value = d.destinatarioTelefono || '';
  document.getElementById('dir-principal').checked = !!d.esPrincipal;
  Modal.open('modal-dir');
}

async function guardarDireccion() {
  const id = document.getElementById('dir-id').value;
  const payload = {
    etiqueta: document.getElementById('dir-etiqueta').value.trim() || null,
    zonaId: parseInt(document.getElementById('dir-zona').value, 10) || null,
    direccionCompleta: document.getElementById('dir-direccion').value.trim(),
    referencia: document.getElementById('dir-ref').value.trim() || null,
    destinatarioNombre: document.getElementById('dir-dest').value.trim() || null,
    destinatarioTelefono: document.getElementById('dir-tel').value.trim() || null,
    esPrincipal: document.getElementById('dir-principal').checked,
  };

  if (!payload.direccionCompleta) {
    Toast.err('La dirección completa es obligatoria');
    return;
  }

  try {
    if (id) {
      await API.put(`/api/cliente/direcciones/${id}`, payload);
      Toast.ok('Dirección actualizada');
    } else {
      await API.post('/api/cliente/direcciones', payload);
      Toast.ok('Dirección registrada');
    }
    Modal.close('modal-dir');
    await cargarDirecciones();
  } catch (e) {
    Toast.err(e.message || 'No se pudo guardar la dirección');
  }
}

async function desactivarDireccion(id) {
  if (!confirm('¿Desactivar esta dirección?')) return;
  try {
    await API.patch(`/api/cliente/direcciones/${id}/desactivar`, {});
    Toast.info('Dirección desactivada');
    await cargarDirecciones();
  } catch (e) {
    Toast.err(e.message || 'No se pudo desactivar');
  }
}

async function marcarPrincipal(id) {
  try {
    await API.patch(`/api/cliente/direcciones/${id}/principal`, {});
    Toast.ok('Dirección principal actualizada');
    await cargarDirecciones();
  } catch (e) {
    Toast.err(e.message || 'No se pudo actualizar dirección principal');
  }
}

async function cargarHistorial() {
  const root = document.getElementById('historial-list');
  if (!root) return;

  try {
    historialPedidos = await API.get('/api/pedidos/mis-pedidos');
  } catch (e) {
    root.innerHTML = `<div class="card panel-error">No se pudo cargar historial: ${e.message}</div>`;
    return;
  }

  if (!historialPedidos.length) {
    root.innerHTML = '<div class="card panel-empty">Aún no tienes pedidos registrados.</div>';
    return;
  }

  root.innerHTML = historialPedidos.map((p) => {
    const itemsTxt = (p.items || [])
      .slice(0, 3)
      .map((it) => `${it.cantidad}x ${it.descripcion}`)
      .join('<br>');
    return `
      <div class="order-card">
        <div class="order-header">
          <div>
            ${badgeEstado(p.estadoId, p.estado)}
            <h3 class="order-code">${p.codigoPedido || ('PED-' + p.pedidoId)}</h3>
            <p class="order-date">${fmt.dt(p.fechaCreacion)}</p>
          </div>
          <div class="text-end">
            <div class="order-amount">${fmt.money(p.total)}</div>
            <button class="btn btn-brand btn-sm mt-1" onclick="reordenarPedido(${p.pedidoId})"><i class="bi bi-arrow-repeat"></i> Reordenar</button>
          </div>
        </div>
        <div class="order-grid">
          <div class="order-items">${itemsTxt || 'Sin ítems'}</div>
          <div class="text-end">
            <a href="carrito.html" class="btn btn-soft btn-sm"><i class="bi bi-cart3"></i> Ir al carrito</a>
          </div>
        </div>
      </div>`;
  }).join('');
}

async function reordenarPedido(pedidoId) {
  try {
    const res = await API.post(`/api/pedidos/${pedidoId}/reordenar`, { limpiarCarrito: true });
    const fallidos = (res.items || []).filter((x) => x.estado === 'RECHAZADO').length;
    const parciales = (res.items || []).filter((x) => x.estado === 'PARCIAL').length;
    let msg = `${res.mensaje}. Agregados: ${res.totalItemsAgregados}`;
    if (parciales > 0 || fallidos > 0) {
      msg += `, parciales: ${parciales}, rechazados: ${fallidos}`;
    }
    Toast.info(msg);
    setTimeout(() => {
      window.location.href = 'carrito.html';
    }, 1000);
  } catch (e) {
    Toast.err(e.message || 'No se pudo reordenar el pedido');
  }
}

async function cargarNotificaciones() {
  const root = document.getElementById('notif-list');
  if (!root) return;
  try {
    const notifs = await API.get('/api/cliente/notificaciones');
    if (!notifs.length) {
      root.innerHTML = '<div class="card panel-empty">No hay notificaciones recientes.</div>';
      return;
    }
    root.innerHTML = notifs.map((n) => `
      <div class="card notif-card">
        <div class="flex-between">
          <strong class="notif-title">${n.asunto || 'Notificación'}</strong>
          <span class="badge ${n.estadoEnvio === 'ENVIADA' ? 'badge-green' : (n.estadoEnvio === 'ERROR' ? 'badge-red' : 'badge-yellow')}">${n.estadoEnvio}</span>
        </div>
        <p class="notif-msg">${n.mensaje}</p>
        <small class="notif-meta">${fmt.dt(n.fechaEnvio)} · canal ${n.canal}</small>
      </div>
    `).join('');
  } catch (e) {
    root.innerHTML = `<div class="card panel-error">No se pudieron cargar notificaciones: ${e.message}</div>`;
  }
}

window.switchTab = switchTab;
window.openDireccionModal = openDireccionModal;
window.editarDireccion = editarDireccion;
window.guardarDireccion = guardarDireccion;
window.desactivarDireccion = desactivarDireccion;
window.marcarPrincipal = marcarPrincipal;
window.reordenarPedido = reordenarPedido;

document.addEventListener('DOMContentLoaded', initPerfil);
