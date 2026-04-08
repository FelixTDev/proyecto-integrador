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

let pedidos = [];
let pedidoSeleccionado = null;

const bodyEl = document.getElementById('pedidos-body');
const searchEl = document.getElementById('pedido-search');
const validarForm = document.getElementById('validar-form');
const motivoEl = document.getElementById('f-motivo');
const btnAprobar = document.getElementById('btn-aprobar');
const btnRechazar = document.getElementById('btn-rechazar');

const modalTitulo = document.getElementById('modal-validar-title');
const modalCliente = document.getElementById('modal-validar-cliente');
const modalEmail = document.getElementById('modal-validar-email');
const modalMonto = document.getElementById('modal-validar-monto');

const comprobanteTitle = document.getElementById('comp-title');
const compResumen = document.getElementById('comp-resumen');
const compItemsBody = document.getElementById('comp-items-body');
const compTotales = document.getElementById('comp-totales');

function estadoBadge(estadoId, estado) {
  const cls = estadoBadgeClass[estadoId] || 'badge-ghost';
  return `<span class="badge ${cls}">${estado}</span>`;
}

function accionHtml(p) {
  const partes = [];
  if (p.estadoId === 1) {
    partes.push(`<button class="btn btn-primary btn-sm" data-action="validar" data-id="${p.id}"><i class="bi bi-check-circle"></i> Validar</button>`);
  }
  partes.push(`<button class="btn btn-ghost btn-sm" data-action="comprobante" data-id="${p.id}"><i class="bi bi-file-earmark-text"></i> Comprobante</button>`);
  return partes.join(' ');
}

function renderTabla(lista) {
  if (!lista.length) {
    bodyEl.innerHTML = `<tr><td colspan="6" class="table-empty-row">No hay pedidos para mostrar.</td></tr>`;
    return;
  }

  bodyEl.innerHTML = lista.map(p => `
    <tr>
      <td><strong>${p.codigoPedido || ('PED-' + p.id)}</strong></td>
      <td class="pedido-date">${fmt.dt(p.fechaCreacion)}</td>
      <td>${p.clienteNombre || '—'}<br><span class="pedido-email">${p.clienteEmail || '—'}</span></td>
      <td class="pedido-total">${fmt.money(p.total)}</td>
      <td>${estadoBadge(p.estadoId, p.estado)}</td>
      <td>${accionHtml(p)}</td>
    </tr>
  `).join('');
}

function applySearch() {
  const q = (searchEl.value || '').trim().toLowerCase();
  if (!q) return renderTabla(pedidos);

  const filtrados = pedidos.filter(p => {
    const codigo = (p.codigoPedido || '').toLowerCase();
    const nombre = (p.clienteNombre || '').toLowerCase();
    const email = (p.clienteEmail || '').toLowerCase();
    const estado = (p.estado || '').toLowerCase();
    return codigo.includes(q) || nombre.includes(q) || email.includes(q) || estado.includes(q) || String(p.id).includes(q);
  });
  renderTabla(filtrados);
}

async function cargarPedidos() {
  const res = await API.get('/api/admin/pedidos');
  pedidos = Array.isArray(res) ? res : (res.data || []);
  applySearch();
}

function abrirValidacion(id) {
  const p = pedidos.find(x => x.id === id);
  if (!p) return;
  pedidoSeleccionado = p;
  modalTitulo.textContent = `Validar Pedido ${p.codigoPedido || ('PED-' + p.id)}`;
  modalCliente.textContent = p.clienteNombre || '—';
  modalEmail.textContent = p.clienteEmail || '—';
  modalMonto.textContent = fmt.money(p.total);
  motivoEl.value = '';
  Modal.open('modal-validar');
}

async function enviarValidacion(aprobar) {
  if (!pedidoSeleccionado) return;
  const motivo = motivoEl.value.trim();
  if (!aprobar && !motivo) {
    Toast.err('Para rechazar, debes ingresar un motivo.');
    return;
  }

  const payload = { aprobar, motivo: motivo || null };
  await API.patch(`/api/admin/pedidos/${pedidoSeleccionado.id}/validacion`, payload);
  Toast.ok(aprobar ? 'Pedido aprobado.' : 'Pedido rechazado.');
  Modal.close('modal-validar');
  await cargarPedidos();
}

async function abrirComprobante(id) {
  const res = await API.get(`/api/pedidos/${id}/comprobante`);
  const c = res?.data || res;

  comprobanteTitle.textContent = `Comprobante ${c.codigoPedido || ('PED-' + c.pedidoId)}`;
  compResumen.innerHTML = `
    <div><span class="comp-label">Pedido</span><br><strong>${c.codigoPedido || ('PED-' + c.pedidoId)}</strong></div>
    <div><span class="comp-label">Fecha</span><br><strong>${fmt.dt(c.fechaCreacion)}</strong></div>
    <div><span class="comp-label">Estado</span><br><strong>${c.estadoActual}</strong></div>
  `;

  compItemsBody.innerHTML = (c.items || []).map(it => `
    <tr>
      <td>${it.descripcion}</td>
      <td class="comp-money-right">${fmt.money(it.precioUnitario)}</td>
      <td class="comp-center">${it.cantidad}</td>
      <td class="comp-money-right">${fmt.money(it.subtotal)}</td>
    </tr>
  `).join('');

  compTotales.innerHTML = `
    <div>Subtotal: <strong>${fmt.money(c.subtotal)}</strong></div>
    <div>Descuento: <strong>${fmt.money(c.descuento)}</strong></div>
    <div>Envío: <strong>${fmt.money(c.costoEnvio)}</strong></div>
    <div>Impuestos: <strong>${fmt.money(c.impuestos)}</strong></div>
    <div class="comp-total-line">Total: <strong class="text-brand-strong">${fmt.money(c.total)}</strong></div>
  `;

  Modal.open('modal-comprobante');
}

bodyEl.addEventListener('click', async (e) => {
  const btn = e.target.closest('button[data-action]');
  if (!btn) return;

  const id = Number(btn.dataset.id);
  const action = btn.dataset.action;

  try {
    if (action === 'validar') abrirValidacion(id);
    if (action === 'comprobante') await abrirComprobante(id);
  } catch (err) {}
});

if (searchEl) searchEl.addEventListener('input', applySearch);
if (validarForm) validarForm.addEventListener('submit', (e) => e.preventDefault());
if (btnAprobar) btnAprobar.addEventListener('click', () => enviarValidacion(true));
if (btnRechazar) btnRechazar.addEventListener('click', () => enviarValidacion(false));

document.getElementById('btn-print-comp')?.addEventListener('click', () => window.print());

cargarPedidos().catch(() => {
  bodyEl.innerHTML = `<tr><td colspan="6" class="table-error-row">No se pudo cargar pedidos.</td></tr>`;
});
