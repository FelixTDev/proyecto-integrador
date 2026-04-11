Auth.requireAdmin();
Auth.requireRole('ROLE_ADMIN');

const state = {
  page: 0,
  size: 10,
  totalPages: 0,
  categorias: [],
  editingId: null
};

function el(id) {
  return document.getElementById(id);
}

function setLoading(on) {
  const spinner = el('loading');
  if (spinner) spinner.style.display = on ? 'block' : 'none';
}

function emptyVariant() {
  return {
    nombreVariante: '',
    precio: '',
    costo: '',
    pesoGramos: '',
    tiempoPrepMin: '',
    stockDisponible: '0',
    activo: true
  };
}

function variantRowHtml(index, data) {
  const v = data || emptyVariant();
  return `
    <div class="row g-2 align-items-end variant-row" data-index="${index}">
      <div class="col-lg-3 field"><label class="label">Variante</label><input type="text" class="form-control" data-field="nombreVariante" value="${v.nombreVariante || ''}" required></div>
      <div class="col-lg-2 field"><label class="label">Precio</label><input type="number" step="0.01" min="0" class="form-control" data-field="precio" value="${v.precio ?? ''}" required></div>
      <div class="col-lg-2 field"><label class="label">Costo</label><input type="number" step="0.01" min="0" class="form-control" data-field="costo" value="${v.costo ?? ''}"></div>
      <div class="col-lg-1 field"><label class="label">Peso(g)</label><input type="number" min="0" class="form-control" data-field="pesoGramos" value="${v.pesoGramos ?? ''}"></div>
      <div class="col-lg-1 field"><label class="label">Prep(min)</label><input type="number" min="0" class="form-control" data-field="tiempoPrepMin" value="${v.tiempoPrepMin ?? ''}"></div>
      <div class="col-lg-1 field"><label class="label">Stock</label><input type="number" min="0" class="form-control" data-field="stockDisponible" value="${v.stockDisponible ?? 0}"></div>
      <div class="col-lg-1 field"><label class="label">Activo</label><select class="form-select" data-field="activo"><option value="true" ${v.activo !== false ? 'selected' : ''}>Si</option><option value="false" ${v.activo === false ? 'selected' : ''}>No</option></select></div>
      <div class="col-lg-1 d-grid"><button type="button" class="btn btn-outline btn-sm" data-remove-variant="${index}"><i class="bi bi-trash"></i></button></div>
    </div>`;
}

function parseNumber(value) {
  if (value === '' || value === null || value === undefined) return null;
  const n = Number(value);
  return Number.isFinite(n) ? n : null;
}

function collectVariantes() {
  return Array.from(document.querySelectorAll('#variant-list .variant-row')).map(function (row) {
    const get = function (field) {
      const input = row.querySelector('[data-field="' + field + '"]');
      return input ? input.value.trim() : '';
    };
    return {
      nombreVariante: get('nombreVariante'),
      precio: parseNumber(get('precio')),
      costo: parseNumber(get('costo')),
      pesoGramos: parseNumber(get('pesoGramos')),
      tiempoPrepMin: parseNumber(get('tiempoPrepMin')),
      stockDisponible: parseNumber(get('stockDisponible')) ?? 0,
      activo: get('activo') === 'true'
    };
  }).filter(function (v) {
    return v.nombreVariante && v.precio !== null;
  });
}

function renderVariantRows(variantes) {
  const root = el('variant-list');
  const list = Array.isArray(variantes) && variantes.length ? variantes : [emptyVariant()];
  root.innerHTML = list.map(function (v, i) { return variantRowHtml(i, v); }).join('');
}

function addVariantRow() {
  const root = el('variant-list');
  const nextIndex = root.querySelectorAll('.variant-row').length;
  root.insertAdjacentHTML('beforeend', variantRowHtml(nextIndex, emptyVariant()));
}

function resetForm() {
  el('prod-form').reset();
  el('f-id').value = '';
  state.editingId = null;
  el('md-title').textContent = 'Nuevo producto';
  renderVariantRows([]);
}

async function loadCategorias() {
  state.categorias = await API.get('/api/catalogo/categorias');
  const select = el('f-cat');
  select.innerHTML = state.categorias.map(function (c) {
    return '<option value="' + c.id + '">' + c.nombre + '</option>';
  }).join('');
}

function badgeDisponibilidad(item) {
  if (item.activo === false) return '<span class="badge badge-red">No vendible</span>';
  if (!item.hayStock) return '<span class="badge badge-yellow">Sin stock</span>';
  return '<span class="badge badge-green">Disponible</span>';
}

function renderPagination() {
  const root = el('pag');
  if (state.totalPages <= 1) {
    root.innerHTML = '';
    return;
  }

  let html = '<button class="btn btn-outline btn-sm" ' + (state.page === 0 ? 'disabled' : '') + ' data-page="' + (state.page - 1) + '"><i class="bi bi-chevron-left"></i></button>';
  for (let i = 0; i < state.totalPages; i += 1) {
    html += '<button class="btn ' + (i === state.page ? 'btn-primary' : 'btn-outline') + ' btn-sm" data-page="' + i + '">' + (i + 1) + '</button>';
  }
  html += '<button class="btn btn-outline btn-sm" ' + (state.page === state.totalPages - 1 ? 'disabled' : '') + ' data-page="' + (state.page + 1) + '"><i class="bi bi-chevron-right"></i></button>';
  root.innerHTML = html;
}

async function loadData() {
  const tbody = el('tbody-prods');
  setLoading(true);
  try {
    const res = await API.get('/api/admin/productos?page=' + state.page + '&size=' + state.size);
    const rows = Array.isArray(res.content) ? res.content : [];
    state.totalPages = Number(res.totalPages || 0);

    if (!rows.length) {
      tbody.innerHTML = '<tr><td colspan="7" class="text-center text-secondary py-4">No hay productos registrados.</td></tr>';
      renderPagination();
      return;
    }

    tbody.innerHTML = rows.map(function (p) {
      const img = p.urlFotoPortada || '/img/placeholder-producto.png';
      return `
        <tr>
          <td class="text-secondary">#${p.id}</td>
          <td>
            <div class="d-flex align-items-center gap-2">
              <img src="${img}" class="prod-thumb" alt="${p.nombre}">
              <div>
                <div class="fw-semibold text-brand-strong">${p.nombre}</div>
                <div class="small text-secondary">${(p.listaAlergenos || []).join(', ') || 'Sin alergenos'}</div>
              </div>
            </div>
          </td>
          <td>${p.nombreCategoria || '-'}</td>
          <td class="price-strong">${fmt.money(p.precioMinimo)}</td>
          <td>${badgeDisponibilidad(p)}</td>
          <td><span class="badge ${p.activo ? 'badge-green' : 'badge-red'}">${p.activo ? 'Activo' : 'Inactivo'}</span></td>
          <td class="d-flex gap-2">
            <button class="btn btn-ghost btn-sm" data-edit="${p.id}"><i class="bi bi-pencil"></i></button>
            <button class="btn btn-outline btn-sm" data-toggle="${p.id}" data-activo="${p.activo}">${p.activo ? 'Desactivar' : 'Activar'}</button>
          </td>
        </tr>`;
    }).join('');

    renderPagination();
  } catch (error) {
    tbody.innerHTML = '<tr><td colspan="7" class="text-center text-danger py-4">No se pudo cargar productos: ' + error.message + '</td></tr>';
  } finally {
    setLoading(false);
  }
}

async function fillEditForm(id) {
  const p = await API.get('/api/catalogo/' + id);
  state.editingId = p.id;
  el('f-id').value = String(p.id);
  el('f-nombre').value = p.nombre || '';
  el('f-desc').value = p.descripcion || '';
  const cat = state.categorias.find(function (c) { return c.nombre === p.nombreCategoria; });
  if (cat) el('f-cat').value = String(cat.id);
  el('f-img').value = p.imagenUrl || '';
  renderVariantRows(p.variantes || []);
  el('md-title').textContent = 'Editar producto #' + p.id;
  Modal.open('modal-form');
}

function buildPayload() {
  const variantes = collectVariantes();
  if (!variantes.length) throw new Error('Debes agregar al menos una variante valida.');

  const categoriaId = Number(el('f-cat').value);
  if (!Number.isFinite(categoriaId)) throw new Error('Categoria invalida.');

  return {
    nombre: el('f-nombre').value.trim(),
    descripcion: el('f-desc').value.trim(),
    categoriaId,
    activo: true,
    alergenoIds: [],
    variantes
  };
}

async function saveProduct(event) {
  event.preventDefault();
  const submit = el('v-btn');
  submit.disabled = true;
  try {
    const payload = buildPayload();
    if (!payload.nombre) throw new Error('El nombre es obligatorio.');

    if (state.editingId) {
      await API.put('/api/admin/productos/' + state.editingId, payload);
      Toast.ok('Producto actualizado.');
    } else {
      await API.post('/api/admin/productos', payload);
      Toast.ok('Producto creado.');
    }
    Modal.close('modal-form');
    await loadData();
  } catch (error) {
    Toast.err(error.message || 'No se pudo guardar.');
  } finally {
    submit.disabled = false;
  }
}

async function toggleProduct(id) {
  try {
    await API.patch('/api/admin/productos/' + id + '/toggle');
    Toast.ok('Estado del producto actualizado.');
    await loadData();
  } catch (error) {
    Toast.err(error.message || 'No se pudo cambiar estado.');
  }
}

function bindEvents() {
  el('btn-open-product').addEventListener('click', function () {
    resetForm();
    Modal.open('modal-form');
  });

  el('btn-add-variant').addEventListener('click', addVariantRow);

  el('variant-list').addEventListener('click', function (event) {
    const remove = event.target.closest('[data-remove-variant]');
    if (!remove) return;
    const rows = el('variant-list').querySelectorAll('.variant-row');
    if (rows.length <= 1) {
      Toast.info('El producto debe tener al menos una variante.');
      return;
    }
    remove.closest('.variant-row').remove();
  });

  el('prod-form').addEventListener('submit', saveProduct);

  el('tbody-prods').addEventListener('click', function (event) {
    const editBtn = event.target.closest('[data-edit]');
    if (editBtn) {
      fillEditForm(Number(editBtn.getAttribute('data-edit'))).catch(function (error) {
        Toast.err(error.message || 'No se pudo cargar detalle del producto.');
      });
      return;
    }

    const toggleBtn = event.target.closest('[data-toggle]');
    if (toggleBtn) {
      toggleProduct(Number(toggleBtn.getAttribute('data-toggle')));
    }
  });

  el('pag').addEventListener('click', function (event) {
    const pageBtn = event.target.closest('[data-page]');
    if (!pageBtn) return;
    const p = Number(pageBtn.getAttribute('data-page'));
    if (!Number.isFinite(p) || p < 0 || p >= state.totalPages) return;
    state.page = p;
    loadData();
  });
}

document.addEventListener('DOMContentLoaded', async function () {
  bindEvents();
  try {
    await loadCategorias();
    await loadData();
  } catch (error) {
    Toast.err(error.message || 'No se pudo inicializar la pantalla de productos.');
  }
});
