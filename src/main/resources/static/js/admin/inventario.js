Auth.requireAdmin();
Auth.requireRole('ROLE_ADMIN');

const state = {
  productos: [],
  variantes: [],
  filtroTexto: '',
  filtroStock: 'TODOS',
  selectedVarianteId: null,
  selectedVarianteLabel: ''
};

function qs(id) {
  return document.getElementById(id);
}

function stockBadge(variant) {
  const stock = Number(variant.stockDisponible || 0);
  if (!variant.activo) return '<span class="badge badge-red">No vendible</span>';
  if (stock <= 0) return '<span class="badge badge-red">Agotado</span>';
  if (stock <= 5) return '<span class="badge badge-yellow">Stock bajo</span>';
  return '<span class="badge badge-green">Stock saludable</span>';
}

function stockClass(variant) {
  const stock = Number(variant.stockDisponible || 0);
  if (!variant.activo || stock <= 0) return 'inv-stock-low';
  if (stock <= 5) return 'inv-stock-low';
  return 'inv-stock-healthy';
}

function flattenVariantes(productos) {
  return productos.flatMap(function (p) {
    const list = Array.isArray(p.variantes) ? p.variantes : [];
    return list.map(function (v) {
      return {
        id: v.id,
        nombreProducto: p.nombre,
        nombreVariante: v.nombreVariante,
        activo: v.activo !== false,
        stockDisponible: Number(v.stockDisponible || 0),
        precio: v.precio,
        costo: v.costo,
        pesoGramos: v.pesoGramos,
        tiempoPrepMin: v.tiempoPrepMin
      };
    });
  });
}

async function loadProductosConVariantes() {
  let page = 0;
  let totalPages = 1;
  const all = [];

  while (page < totalPages) {
    const res = await API.get('/api/admin/productos?page=' + page + '&size=50');
    all.push(...(res.content || []));
    totalPages = Number(res.totalPages || 0);
    page += 1;
  }

  const detalles = await Promise.all(all.map(function (p) {
    return API.get('/api/catalogo/' + p.id).catch(function () { return null; });
  }));

  state.productos = detalles.filter(Boolean);
  state.variantes = flattenVariantes(state.productos);
}

function getFiltradas() {
  const q = state.filtroTexto.toLowerCase();
  return state.variantes.filter(function (v) {
    if (state.filtroStock === 'CRITICO' && Number(v.stockDisponible) > 5) return false;
    if (state.filtroStock === 'AGOTADO' && Number(v.stockDisponible) > 0) return false;

    if (!q) return true;
    return (
      String(v.id).includes(q) ||
      (v.nombreProducto || '').toLowerCase().includes(q) ||
      (v.nombreVariante || '').toLowerCase().includes(q)
    );
  });
}

function renderInventario() {
  const tbody = qs('inv-tbody');
  const rows = getFiltradas();

  if (!rows.length) {
    tbody.innerHTML = '<tr><td colspan="6" class="text-center text-secondary py-4">No hay variantes para el filtro actual.</td></tr>';
    return;
  }

  tbody.innerHTML = rows.map(function (v) {
    const meta = [
      v.pesoGramos ? (v.pesoGramos + ' g') : null,
      v.tiempoPrepMin ? (v.tiempoPrepMin + ' min prep') : null,
      v.costo ? ('costo ' + fmt.money(v.costo)) : null
    ].filter(Boolean).join(' | ');

    return `
      <tr>
        <td class="inv-id">VAR-${v.id}</td>
        <td>
          <strong class="text-brand-strong">${v.nombreProducto}</strong>
          <span class="inv-meta">| ${v.nombreVariante}</span>
          <div class="small text-secondary">${meta || '-'}</div>
        </td>
        <td><span class="${stockClass(v)}">${v.stockDisponible}</span> un.</td>
        <td>${stockBadge(v)}</td>
        <td>${fmt.money(v.precio)}</td>
        <td><button class="btn btn-outline btn-sm" data-ajuste="${v.id}"><i class="bi bi-tools"></i> Ajustar</button></td>
      </tr>`;
  }).join('');
}

function openAjuste(id) {
  const v = state.variantes.find(function (x) { return x.id === id; });
  if (!v) return;
  state.selectedVarianteId = id;
  state.selectedVarianteLabel = v.nombreProducto + ' - ' + v.nombreVariante;
  qs('aj-var').value = 'VAR-' + id + ': ' + state.selectedVarianteLabel;
  qs('aj-cantidad').value = '1';
  qs('aj-motivo').value = '';
  qs('aj-tipo').value = 'ENTRADA';
  Modal.open('modal-inv');
}

async function submitAjuste(event) {
  event.preventDefault();
  if (!state.selectedVarianteId) return;

  const payload = {
    tipo: qs('aj-tipo').value,
    cantidad: Number(qs('aj-cantidad').value),
    motivo: qs('aj-motivo').value.trim() || null
  };

  if (!Number.isFinite(payload.cantidad) || payload.cantidad < 1) {
    Toast.err('La cantidad debe ser mayor o igual a 1.');
    return;
  }

  const submit = qs('btn-ajuste-submit');
  submit.disabled = true;
  try {
    await API.post('/api/admin/variantes/' + state.selectedVarianteId + '/inventario', payload);
    Toast.ok('Movimiento de inventario registrado.');
    Modal.close('modal-inv');
    await loadProductosConVariantes();
    renderInventario();
  } catch (error) {
    Toast.err(error.message || 'No se pudo registrar movimiento.');
  } finally {
    submit.disabled = false;
  }
}

function bindEvents() {
  qs('inv-search').addEventListener('input', function (event) {
    state.filtroTexto = event.target.value.trim();
    renderInventario();
  });

  qs('inv-stock-filter').addEventListener('change', function (event) {
    state.filtroStock = event.target.value;
    renderInventario();
  });

  qs('btn-ajuste-open').addEventListener('click', function () {
    if (!state.variantes.length) {
      Toast.info('No hay variantes disponibles para ajustar.');
      return;
    }
    openAjuste(state.variantes[0].id);
  });

  qs('inv-tbody').addEventListener('click', function (event) {
    const btn = event.target.closest('[data-ajuste]');
    if (!btn) return;
    openAjuste(Number(btn.getAttribute('data-ajuste')));
  });

  qs('form-inv-ajuste').addEventListener('submit', submitAjuste);
}

document.addEventListener('DOMContentLoaded', async function () {
  bindEvents();
  try {
    await loadProductosConVariantes();
    renderInventario();
  } catch (error) {
    Toast.err(error.message || 'No se pudo cargar inventario.');
  }
});
