Auth.requireAdmin();

const ESTADOS = [
  { id: 1, nombre: 'Pendiente de pago', color: 'var(--yellow)' },
  { id: 2, nombre: 'Pago confirmado', color: 'var(--blue)' },
  { id: 3, nombre: 'En preparacion', color: 'var(--blue)' },
  { id: 4, nombre: 'Listo para recoger', color: 'var(--gold)' },
  { id: 5, nombre: 'En ruta', color: 'var(--gold)' },
  { id: 6, nombre: 'Entregado', color: 'var(--green)' },
  { id: 7, nombre: 'Cancelado', color: 'var(--red)' },
  { id: 8, nombre: 'Rechazado', color: 'var(--red)' },
];

let pedidos = [];
let draggedPedidoId = null;

function escapeHtml(txt) {
  return String(txt || '').replace(/[&<>"]/g, (ch) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[ch]));
}

function buildCard(p) {
  return `
    <div class="k-card" draggable="true" data-pedido-id="${p.id}">
      <span class="k-card-time">${fmt.dt(p.fechaCreacion)}</span>
      <div class="k-card-id">${escapeHtml(p.codigoPedido || ('PED-' + p.id))}</div>
      <div class="k-card-client"><i class="bi bi-person"></i> ${escapeHtml(p.clienteNombre || 'Sin nombre')}</div>
      <div class="kanban-email">${escapeHtml(p.clienteEmail || '—')}</div>
      <div class="kanban-total">${fmt.money(p.total)}</div>
    </div>
  `;
}

function renderBoard() {
  for (const e of ESTADOS) {
    const col = document.querySelector(`.kanban-col[data-estado-id="${e.id}"]`);
    if (!col) continue;

    const body = col.querySelector('.kanban-body');
    const count = col.querySelector('.k-count');

    const lista = pedidos.filter(p => p.estadoId === e.id);
    body.innerHTML = lista.map(buildCard).join('');
    count.textContent = String(lista.length);
  }

  hookDragEvents();
}

function hookDragEvents() {
  document.querySelectorAll('.k-card').forEach(card => {
    card.addEventListener('dragstart', (ev) => {
      draggedPedidoId = Number(card.dataset.pedidoId);
      card.classList.add('dragging');
      ev.dataTransfer.setData('text/plain', String(draggedPedidoId));
    });
    card.addEventListener('dragend', () => {
      card.classList.remove('dragging');
      document.querySelectorAll('.kanban-col').forEach(c => c.classList.remove('drag-over'));
    });
  });

  document.querySelectorAll('.kanban-col').forEach(col => {
    col.addEventListener('dragover', (ev) => {
      ev.preventDefault();
      col.classList.add('drag-over');
    });

    col.addEventListener('dragleave', () => {
      col.classList.remove('drag-over');
    });

    col.addEventListener('drop', async (ev) => {
      ev.preventDefault();
      col.classList.remove('drag-over');

      const pedidoId = Number(ev.dataTransfer.getData('text/plain') || draggedPedidoId);
      const estadoId = Number(col.dataset.estadoId);
      if (!pedidoId || !estadoId) return;

      const pedido = pedidos.find(p => p.id === pedidoId);
      if (!pedido || pedido.estadoId === estadoId) return;

      const nombreDestino = ESTADOS.find(x => x.id === estadoId)?.nombre || 'nuevo estado';

      try {
        await API.patch(`/api/admin/pedidos/${pedidoId}/estado`, {
          estadoId,
          observacion: `Cambio de estado desde tablero: ${nombreDestino}`,
        });
        Toast.ok(`Pedido movido a ${nombreDestino}.`);
        await cargarPedidos();
      } catch (err) {
        Toast.err('No se pudo cambiar el estado. Revisa la transición permitida.');
      }
    });
  });
}

async function cargarPedidos() {
  const res = await API.get('/api/admin/pedidos');
  pedidos = Array.isArray(res) ? res : (res.data || []);
  renderBoard();
}

cargarPedidos().catch(() => {
  Toast.err('No se pudo cargar el tablero de pedidos.');
});
