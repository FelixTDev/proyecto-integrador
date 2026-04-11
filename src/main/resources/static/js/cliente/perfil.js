const perfilState = {
  zonas: [],
  direcciones: [],
  notificaciones: [],
  reclamos: []
};

function px(id) {
  return document.getElementById(id);
}

function escapeHtml(v) {
  return String(v || '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

function setTab(tab) {
  document.querySelectorAll('.p-content').forEach((c) => c.classList.remove('active'));
  document.querySelectorAll('#perfil-tabs-nav .p-nav-link[data-tab]').forEach((b) => b.classList.remove('active'));
  px('tab-' + tab)?.classList.add('active');
  document.querySelector('#perfil-tabs-nav .p-nav-link[data-tab="' + tab + '"]')?.classList.add('active');
}

async function loadProfile() {
  try {
    const data = await API.get('/api/auth/me');
    px('p-name').textContent = data.nombre || '-';
    px('p-email').textContent = data.email || '-';
    const initials = (data.nombre || 'CC').split(' ').map((w) => w[0]).join('').slice(0, 2).toUpperCase();
    px('p-avatar').textContent = initials;
  } catch (error) {
    Toast.err(error.message || 'No se pudo cargar perfil.');
  }
}

function estadoBadge(estado) {
  const map = {
    Pendiente: 'bg-warning text-dark',
    'Pago confirmado': 'bg-info',
    'En preparacion': 'bg-primary',
    'Listo para entrega': 'bg-success',
    'En camino': 'bg-info',
    Entregado: 'bg-success',
    Cancelado: 'bg-danger',
    Rechazado: 'bg-danger'
  };
  return map[estado] || 'bg-secondary';
}

function reclamoBadge(estado) {
  return { ABIERTO: 'bg-warning text-dark', EN_REVISION: 'bg-info', RESUELTO: 'bg-success', CERRADO: 'bg-secondary' }[estado] || 'bg-secondary';
}

async function loadHistorial() {
  const root = px('historial-list');
  try {
    const pedidos = await API.get('/api/pedidos/mis-pedidos');
    if (!pedidos.length) {
      root.innerHTML = '<p class="text-secondary">No tienes pedidos aun.</p>';
      return;
    }

    root.innerHTML = pedidos.map((p) => `
      <div class="card p-3 mb-3">
        <div class="d-flex flex-wrap justify-content-between align-items-start gap-2">
          <div>
            <strong>${escapeHtml(p.codigoPedido || '#' + p.pedidoId)}</strong>
            <span class="badge ${estadoBadge(p.estado)} ms-2">${escapeHtml(p.estado || '-')}</span>
          </div>
          <small class="text-secondary">${fmt.dt(p.fechaCreacion)}</small>
        </div>
        <p class="mb-1 mt-2 text-secondary small">${(p.items || []).map((i) => escapeHtml(i.descripcion)).join(', ') || 'Ver detalles'}</p>
        <div class="d-flex flex-wrap gap-2 mt-2">
          <span class="fw-bold">${fmt.money(p.total)}</span>
          ${(p.estado === 'Pendiente' || p.estado === 'En preparacion' || p.estado === 'Pago confirmado') ? `<a href="personalizar.html?pedidoId=${p.pedidoId}" class="btn btn-sm btn-outline-brand"><i class="bi bi-magic"></i> Personalizar</a>` : ''}
          <button class="btn btn-sm btn-outline-brand ms-auto" data-comprobante="${p.pedidoId}"><i class="bi bi-receipt"></i> Comprobante</button>
          <button class="btn btn-sm btn-brand" data-reordenar="${p.pedidoId}"><i class="bi bi-arrow-repeat"></i> Reordenar</button>
          <button class="btn btn-sm btn-outline-secondary" data-chat="${p.pedidoId}"><i class="bi bi-chat-dots"></i> Chat</button>
        </div>
      </div>`).join('');
  } catch (error) {
    root.innerHTML = '<p class="text-danger">Error al cargar historial.</p>';
  }
}

async function verComprobante(pedidoId) {
  try {
    const data = await API.get('/api/pedidos/' + pedidoId + '/comprobante');
    const popup = window.open('', '_blank', 'width=800,height=760');
    const rows = (data.items || []).map((i) => `
      <tr>
        <td>${escapeHtml(i.nombre || i.descripcion)}</td>
        <td>${i.cantidad}</td>
        <td>${fmt.money(i.precioUnitario)}</td>
        <td>${fmt.money(i.subtotalLinea)}</td>
      </tr>`).join('');

    popup.document.write(`
      <html><head><title>Comprobante ${escapeHtml(data.codigoPedido || '#' + data.pedidoId)}</title>
      <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"></head>
      <body class="p-4">
      <h4>Comprobante ${escapeHtml(data.codigoPedido || '#' + data.pedidoId)}</h4>
      <p><strong>Fecha:</strong> ${fmt.dt(data.fechaCreacion)}<br><strong>Estado:</strong> ${escapeHtml(data.estadoActual || '-')}</p>
      <table class="table table-sm"><thead><tr><th>Producto</th><th>Cant.</th><th>P.U.</th><th>Sub</th></tr></thead><tbody>${rows}</tbody></table>
      <p class="text-end"><strong>Subtotal:</strong> ${fmt.money(data.subtotal)}<br><strong>Envio:</strong> ${fmt.money(data.costoEnvio)}<br><strong class="fs-5">Total: ${fmt.money(data.total)}</strong></p>
      <button id="btn-print-comprobante" class="btn btn-primary btn-sm">Imprimir</button>
      </body></html>`);
    popup.document.getElementById('btn-print-comprobante')?.addEventListener('click', () => popup.print());
  } catch (error) {
    Toast.err(error.message || 'No se pudo abrir comprobante.');
  }
}

async function reordenarPedido(pedidoId) {
  try {
    const result = await API.post('/api/pedidos/' + pedidoId + '/reordenar', {});
    Toast.ok('Productos agregados al carrito (' + (result.itemsAgregados || 0) + ').');
  } catch (error) {
    Toast.err(error.message || 'No se pudo reordenar.');
  }
}

async function loadDirecciones() {
  try {
    const [zonas, dirs] = await Promise.all([
      API.get('/api/cliente/direcciones/zonas'),
      API.get('/api/cliente/direcciones')
    ]);
    perfilState.zonas = zonas || [];
    perfilState.direcciones = dirs || [];
  } catch {
    perfilState.zonas = [];
    perfilState.direcciones = [];
  }
  renderDirecciones();
}

function renderDirecciones() {
  const root = px('dir-list');
  if (!perfilState.direcciones.length) {
    root.innerHTML = '<p class="text-secondary">No tienes direcciones registradas.</p>';
    return;
  }

  root.innerHTML = perfilState.direcciones.map((d) => `
    <div class="card p-3 mb-2 ${d.esPrincipal ? 'border-warning' : ''}">
      <div class="d-flex justify-content-between align-items-start">
        <div>
          <strong>${escapeHtml(d.etiqueta || 'Sin etiqueta')}</strong>
          ${d.esPrincipal ? '<span class="badge bg-warning text-dark ms-2">Principal</span>' : ''}
          ${!d.activo ? '<span class="badge bg-secondary ms-2">Inactiva</span>' : ''}
        </div>
        <div class="d-flex gap-1">
          ${d.activo && !d.esPrincipal ? `<button class="btn btn-sm btn-outline-warning" data-principal="${d.id}" title="Hacer principal"><i class="bi bi-star"></i></button>` : ''}
          <button class="btn btn-sm btn-outline-brand" data-edit-dir="${d.id}" title="Editar"><i class="bi bi-pencil"></i></button>
          ${d.activo ? `<button class="btn btn-sm btn-outline-danger" data-del-dir="${d.id}" title="Eliminar"><i class="bi bi-trash"></i></button>` : ''}
        </div>
      </div>
      <p class="text-secondary small mb-0 mt-1">${escapeHtml(d.direccionCompleta)} ${d.zonaNombre ? '(' + escapeHtml(d.zonaNombre) + ')' : ''}</p>
    </div>`).join('');
}

function openDireccionModal(dir = null) {
  px('dir-modal-title').textContent = dir ? 'Editar direccion' : 'Nueva direccion';
  px('dir-id').value = dir?.id || '';
  px('dir-etiqueta').value = dir?.etiqueta || '';
  px('dir-direccion').value = dir?.direccionCompleta || '';
  px('dir-ref').value = dir?.referencia || '';
  px('dir-dest').value = dir?.destinatarioNombre || '';
  px('dir-tel').value = dir?.destinatarioTelefono || '';
  px('dir-principal').checked = !!dir?.esPrincipal;
  px('dir-zona').innerHTML = '<option value="">Sin zona</option>' + perfilState.zonas.map((z) => `<option value="${z.id}" ${z.id === dir?.zonaId ? 'selected' : ''}>${escapeHtml(z.nombreDistrito)} - ${fmt.money(z.costoDelivery)}</option>`).join('');
  Modal.open('modal-dir');
}

async function guardarDireccion() {
  const id = px('dir-id').value;
  const body = {
    etiqueta: px('dir-etiqueta').value,
    direccionCompleta: px('dir-direccion').value,
    referencia: px('dir-ref').value,
    destinatarioNombre: px('dir-dest').value,
    destinatarioTelefono: px('dir-tel').value,
    zonaId: px('dir-zona').value || null,
    esPrincipal: px('dir-principal').checked
  };

  try {
    if (id) await API.put('/api/cliente/direcciones/' + id, body);
    else await API.post('/api/cliente/direcciones', body);
    Toast.ok('Direccion guardada.');
    Modal.close('modal-dir');
    await loadDirecciones();
  } catch (error) {
    Toast.err(error.message || 'No se pudo guardar la direccion.');
  }
}

async function desactivarDireccion(dirId) {
  try {
    await API.delete('/api/cliente/direcciones/' + dirId);
    Toast.ok('Direccion eliminada.');
    await loadDirecciones();
  } catch (error) {
    Toast.err(error.message || 'No se pudo eliminar direccion.');
  }
}

async function marcarPrincipal(dirId) {
  try {
    await API.patch('/api/cliente/direcciones/' + dirId + '/principal');
    Toast.ok('Direccion marcada como principal.');
    await loadDirecciones();
  } catch (error) {
    Toast.err(error.message || 'No se pudo actualizar direccion principal.');
  }
}

async function loadPuntos() {
  try {
    const data = await API.get('/api/cliente/puntos');
    px('puntos-saldo').textContent = data.saldo || 0;
    px('p-puntos').innerHTML = `<i class="bi bi-star-fill me-1"></i>${data.saldo || 0} Puntos`;

    const root = px('puntos-list');
    if (!data.movimientos || !data.movimientos.length) {
      root.innerHTML = '<p class="text-secondary">Sin movimientos aun.</p>';
      return;
    }

    root.innerHTML = data.movimientos.map((m) => `
      <div class="card p-3 mb-2">
        <div class="d-flex justify-content-between">
          <div>
            <span class="badge ${m.tipo === 'GANADO' ? 'bg-success' : m.tipo === 'CANJEADO' ? 'bg-warning text-dark' : 'bg-secondary'}">${escapeHtml(m.tipo)}</span>
            <span class="ms-2">${escapeHtml(m.descripcion || '-')}</span>
          </div>
          <div class="text-end">
            <strong class="${m.puntos > 0 ? 'text-success' : 'text-danger'}">${m.puntos > 0 ? '+' : ''}${m.puntos}</strong>
            <small class="d-block text-secondary">${fmt.dt(m.fecha)}</small>
          </div>
        </div>
      </div>`).join('');
  } catch (error) {
    px('puntos-saldo').textContent = '-';
    px('puntos-list').innerHTML = '<p class="text-secondary">No se pudo cargar los puntos.</p>';
  }
}

async function loadPedidosForReclamo() {
  try {
    const pedidos = await API.get('/api/pedidos/mis-pedidos');
    px('reclamo-pedido').innerHTML = pedidos.map((p) => `<option value="${p.pedidoId}">${escapeHtml(p.codigoPedido || '#' + p.pedidoId)} - ${fmt.dt(p.fechaCreacion)}</option>`).join('');
  } catch {
    px('reclamo-pedido').innerHTML = '<option value="">Sin pedidos disponibles</option>';
  }
}

async function enviarReclamo() {
  const pedidoId = Number(px('reclamo-pedido').value);
  const body = {
    pedidoId,
    tipo: px('reclamo-tipo').value,
    descripcion: px('reclamo-desc').value
  };
  try {
    await API.post('/api/cliente/reclamos', body);
    Toast.ok('Reclamo registrado exitosamente.');
    px('reclamo-form').reset();
    px('reclamo-form-card').style.display = 'none';
    await loadReclamos();
  } catch (error) {
    Toast.err(error.message || 'No se pudo registrar reclamo.');
  }
}

async function loadReclamos() {
  const root = px('reclamos-list');
  try {
    perfilState.reclamos = await API.get('/api/cliente/reclamos');
    if (!perfilState.reclamos.length) {
      root.innerHTML = '<p class="text-secondary">No tienes reclamos registrados.</p>';
      return;
    }

    root.innerHTML = perfilState.reclamos.map((r) => `
      <div class="card p-3 mb-2">
        <div class="d-flex justify-content-between align-items-start">
          <div>
            <strong>Reclamo #${r.id}</strong>
            <span class="badge ${reclamoBadge(r.estado)} ms-2">${escapeHtml(r.estado)}</span>
            <span class="badge bg-secondary ms-1">${escapeHtml(r.tipo)}</span>
          </div>
          <small class="text-secondary">${fmt.dt(r.fechaCreacion)}</small>
        </div>
        <p class="text-secondary small mb-1 mt-2">${escapeHtml(r.descripcion)}</p>
        ${r.detalleResolucion ? `<p class="small mb-0"><strong>Resolucion:</strong> ${escapeHtml(r.detalleResolucion)}</p>` : ''}
        ${r.montoReembolso > 0 ? `<p class="small text-success mb-0"><strong>Reembolso:</strong> ${fmt.money(r.montoReembolso)}</p>` : ''}
      </div>`).join('');
  } catch {
    root.innerHTML = '<p class="text-danger">Error al cargar reclamos.</p>';
  }
}

async function loadNotificaciones() {
  const root = px('notif-list');
  try {
    perfilState.notificaciones = await API.get('/api/cliente/notificaciones');
    if (!perfilState.notificaciones.length) {
      root.innerHTML = '<p class="text-secondary">Sin notificaciones.</p>';
      return;
    }

    root.innerHTML = perfilState.notificaciones.map((n) => `
      <button type="button" class="card p-3 mb-2 w-100 text-start ${n.leida ? 'opacity-75' : ''}" data-notif="${n.id}">
        <div class="d-flex justify-content-between align-items-start">
          <div>
            ${!n.leida ? '<i class="bi bi-circle-fill text-warning me-2" style="font-size:0.5rem"></i>' : ''}
            <strong>${escapeHtml(n.asunto || 'Notificacion')}</strong>
          </div>
          <small class="text-secondary">${fmt.dt(n.fechaEnvio)}</small>
        </div>
        <p class="text-secondary small mb-0 mt-1">${escapeHtml(n.mensaje)}</p>
      </button>`).join('');
  } catch {
    root.innerHTML = '<p class="text-danger">Error al cargar notificaciones.</p>';
  }
}

async function marcarNotifLeida(notifId) {
  try {
    await API.patch('/api/cliente/notificaciones/' + notifId + '/leida');
    await loadNotificaciones();
  } catch (error) {
    Toast.err(error.message || 'No se pudo marcar notificacion.');
  }
}

function bindEvents() {
  document.getElementById('perfil-tabs-nav').addEventListener('click', (event) => {
    const btn = event.target.closest('[data-tab]');
    if (!btn) return;
    const tab = btn.getAttribute('data-tab');
    setTab(tab);
    window.location.hash = 'tab-' + tab;
  });

  px('perfil-logout').addEventListener('click', doLogout);
  px('btn-open-direccion').addEventListener('click', () => openDireccionModal());
  px('btn-save-dir').addEventListener('click', guardarDireccion);

  px('historial-list').addEventListener('click', (event) => {
    const cb = event.target.closest('[data-comprobante]');
    if (cb) return verComprobante(Number(cb.getAttribute('data-comprobante')));
    const reo = event.target.closest('[data-reordenar]');
    if (reo) return reordenarPedido(Number(reo.getAttribute('data-reordenar')));
    const chat = event.target.closest('[data-chat]');
    if (chat) window.location.href = '/pages/cliente/chat.html?pedidoId=' + Number(chat.getAttribute('data-chat'));
  });

  px('dir-list').addEventListener('click', (event) => {
    const edit = event.target.closest('[data-edit-dir]');
    if (edit) {
      const id = Number(edit.getAttribute('data-edit-dir'));
      const dir = perfilState.direcciones.find((d) => d.id === id);
      if (dir) openDireccionModal(dir);
      return;
    }

    const del = event.target.closest('[data-del-dir]');
    if (del) {
      desactivarDireccion(Number(del.getAttribute('data-del-dir')));
      return;
    }

    const pri = event.target.closest('[data-principal]');
    if (pri) {
      marcarPrincipal(Number(pri.getAttribute('data-principal')));
    }
  });

  px('btn-open-reclamo').addEventListener('click', async () => {
    const card = px('reclamo-form-card');
    const show = card.style.display === 'none';
    card.style.display = show ? 'block' : 'none';
    if (show) await loadPedidosForReclamo();
  });

  px('reclamo-form').addEventListener('submit', (event) => {
    event.preventDefault();
    enviarReclamo();
  });

  px('notif-list').addEventListener('click', (event) => {
    const card = event.target.closest('[data-notif]');
    if (!card) return;
    marcarNotifLeida(Number(card.getAttribute('data-notif')));
  });
}

async function initPerfil() {
  if (!Auth.requireClient()) return;
  bindEvents();

  await Promise.all([
    loadProfile(),
    loadHistorial(),
    loadDirecciones(),
    loadPuntos(),
    loadReclamos(),
    loadNotificaciones()
  ]);

  const hashTab = (window.location.hash || '').replace('#tab-', '').trim();
  if (hashTab && px('tab-' + hashTab)) {
    setTab(hashTab);
  }
}

document.addEventListener('DOMContentLoaded', initPerfil);
