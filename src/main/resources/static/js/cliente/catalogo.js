const state = {
  page: 0,
  size: 12,
  categoriaId: null,
  filtroTexto: '',
  totalElements: 0,
  totalPages: 0,
  selectedVariante: null,
  promociones: []
};

let productosPagina = [];

function escapeHtml(value) {
  return String(value || '').replace(/[&<>"']/g, function (ch) {
    return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[ch];
  });
}

async function loadPromocionesVigentes() {
  try {
    state.promociones = await API.get('/api/promociones/vigentes', { auth: false, retryOn401: false });
  } catch {
    state.promociones = [];
  }

  const catalog = document.getElementById('catalogo');
  if (!catalog) return;

  let block = document.getElementById('promo-vigentes');
  if (!block) {
    block = document.createElement('div');
    block.id = 'promo-vigentes';
    block.className = 'mb-4';
    const container = catalog.querySelector('.container');
    if (container) container.insertBefore(block, container.firstChild.nextSibling);
  }

  if (!state.promociones.length) {
    block.innerHTML = '';
    return;
  }

  block.innerHTML = `
    <div class="card p-3">
      <div class="d-flex align-items-center justify-content-between flex-wrap gap-2 mb-2">
        <h3 class="h6 mb-0 text-brand-strong"><i class="bi bi-tag me-2"></i>Promociones vigentes</h3>
      </div>
      <div class="d-flex flex-wrap gap-2">
        ${state.promociones.map(function (p) {
          const tag = p.codigoCupon ? ('Cupón ' + p.codigoCupon) : p.tipoDescuento;
          return '<span class="badge badge-yellow">' + escapeHtml(p.nombre) + ' - ' + escapeHtml(tag) + '</span>';
        }).join('')}
      </div>
    </div>`;
}

async function loadCategorias() {
  try {
    const categorias = await API.get('/api/catalogo/categorias', { auth: false, retryOn401: false });
    const fc = document.getElementById('cat-filters');
    fc.innerHTML =
      '<button class="btn ' + (!state.categoriaId ? 'btn-gold' : 'btn-soft') + '" data-category="">Todos</button>' +
      (categorias || [])
        .map(function (c) {
          return '<button class="btn ' + (state.categoriaId === c.id ? 'btn-gold' : 'btn-soft') + '" data-category="' + c.id + '">' + escapeHtml(c.nombre) + '</button>';
        })
        .join('');
  } catch {
    const fc = document.getElementById('cat-filters');
    if (fc) fc.innerHTML = '';
  }
}

function setCategory(id) {
  state.categoriaId = id;
  state.page = 0;
  loadCategorias();
  loadProductos();
}

function onSearchInput(ev) {
  state.filtroTexto = (ev.target.value || '').trim().toLowerCase();
  renderProductos();
}

function disponibilidadBadge(p) {
  if (p.activo === false) return '<span class="badge badge-red">No vendible</span>';
  if (!p.hayStock) return '<span class="badge badge-yellow">Agotado</span>';
  return '<span class="badge badge-green">Disponible</span>';
}

async function loadProductos() {
  const grid = document.getElementById('grid-productos');
  grid.innerHTML = Array(4)
    .fill('<div class="col"><div class="card p-3 h-100"><div class="skeleton skeleton-media"></div><div class="skeleton skeleton-title"></div><div class="skeleton skeleton-line"></div><div class="skeleton skeleton-line-short"></div></div></div>')
    .join('');

  let url = '/api/catalogo?page=' + state.page + '&size=' + state.size;
  if (state.categoriaId) url += '&categoria=' + state.categoriaId;

  try {
    const pageData = await API.get(url, { auth: false, retryOn401: false });
    state.totalElements = pageData.totalElements || 0;
    state.totalPages = pageData.totalPages || 0;
    productosPagina = pageData.content || [];

    const filteredCount = getProductosFiltrados().length;
    document.getElementById('cat-counts').textContent =
      state.filtroTexto ? (filteredCount + ' de ' + state.totalElements + ' productos') : (state.totalElements + ' productos');

    renderProductos();
    renderPagination();
  } catch {
    grid.innerHTML = '<div class="col-12"><div class="empty"><i class="bi bi-exclamation-triangle"></i><p>Error cargando catalogo.</p></div></div>';
  }
}

function getProductosFiltrados() {
  const q = state.filtroTexto;
  if (!q) return productosPagina;
  return productosPagina.filter(function (p) {
    const nombre = (p.nombre || '').toLowerCase();
    const desc = (p.descripcion || '').toLowerCase();
    const cat = (p.nombreCategoria || '').toLowerCase();
    return nombre.includes(q) || desc.includes(q) || cat.includes(q);
  });
}

function renderProductos() {
  const grid = document.getElementById('grid-productos');
  const lista = getProductosFiltrados();

  if (!lista.length) {
    grid.innerHTML = '<div class="col-12"><div class="empty"><i class="bi bi-box2"></i><p>No hay productos para el filtro seleccionado.</p></div></div>';
    return;
  }

  grid.innerHTML = lista.map(function (p) {
    const img = p.imagenUrl || p.urlFotoPortada || 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=400&q=80';
    const alergenos = Array.isArray(p.listaAlergenos) && p.listaAlergenos.length ? p.listaAlergenos.join(', ') : 'Sin alergenos';
    return `
      <div class="col">
        <div class="card card-hover h-100 p-3">
          <div class="product-img-wrapper mb-3">
            <img src="${img}" class="product-img" alt="${escapeHtml(p.nombre)}">
          </div>
          <div class="d-flex gap-2 mb-2 flex-wrap">
            ${disponibilidadBadge(p)}
            <span class="badge badge-gold">${escapeHtml(p.nombreCategoria || 'General')}</span>
          </div>
          <h3 class="product-title">${escapeHtml(p.nombre)}</h3>
          <p class="product-desc">${escapeHtml(p.descripcion || '')}</p>
          <p class="small text-secondary mb-3"><strong>Alergenos:</strong> ${escapeHtml(alergenos)}</p>
          <div class="d-flex justify-content-between align-items-center mt-auto">
            <span class="product-price">${fmt.money(p.precioMinimo)}</span>
            <button class="btn btn-outline-brand btn-sm" data-detalle="${p.id}"><i class="bi bi-eye"></i> Detalle</button>
          </div>
        </div>
      </div>`;
  }).join('');
}

function renderPagination() {
  const pag = document.getElementById('pagination');
  if (state.totalPages <= 1) {
    pag.innerHTML = '';
    return;
  }

  let html = '<button class="btn btn-outline-brand" ' + (state.page === 0 ? 'disabled' : '') + ' data-page="' + (state.page - 1) + '"><i class="bi bi-chevron-left"></i> Anteriores</button>';
  html += '<span class="pagination-meta">Pag ' + (state.page + 1) + ' de ' + state.totalPages + '</span>';
  html += '<button class="btn btn-outline-brand" ' + (state.page === state.totalPages - 1 ? 'disabled' : '') + ' data-page="' + (state.page + 1) + '">Siguientes <i class="bi bi-chevron-right"></i></button>';
  pag.innerHTML = html;
}

function setPage(p) {
  if (p >= 0 && p < state.totalPages) {
    state.page = p;
    loadProductos();
    document.getElementById('catalogo').scrollIntoView({ behavior: 'smooth' });
  }
}

async function openDetalle(id) {
  state.selectedVariante = null;
  const varsEl = document.getElementById('md-vars');
  varsEl.innerHTML = '<div class="spin modal-loader"><i class="bi bi-arrow-repeat"></i></div>';
  document.getElementById('md-add-btn').disabled = true;
  Modal.open('modal-detalle');

  try {
    const p = await API.get('/api/catalogo/' + id, { auth: false, retryOn401: false });
    document.getElementById('md-title').textContent = p.nombre;
    document.getElementById('md-cat').textContent = p.nombreCategoria || 'General';
    document.getElementById('md-img').src = p.imagenUrl || p.urlFotoPortada || 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=400&q=80';
    document.getElementById('md-desc').textContent = p.descripcion || 'Sin descripcion';

    const al = document.getElementById('md-alergenos');
    al.innerHTML = (p.listaAlergenos || []).map(function (a) {
      return '<span class="badge badge-yellow allergen-badge-xs"><i class="bi bi-exclamation-triangle"></i> ' + escapeHtml(a) + '</span>';
    }).join('');

    const vars = p.variantes || [];
    if (!vars.length) {
      varsEl.innerHTML = '<p class="text-xs-muted">No hay variantes disponibles.</p>';
      return;
    }

    varsEl.innerHTML = vars.map(function (v) {
      const disponible = v.activo && v.stockDisponible > 0;
      return `
        <button type="button" class="var-item ${disponible ? '' : 'var-item-disabled'}" data-variante="${v.id}" ${disponible ? '' : 'disabled'}>
          <span class="var-item-name">${escapeHtml(v.nombreVariante)} ${disponible ? '' : '(Agotado)'}</span>
          <span class="var-item-price">${fmt.money(v.precio)}</span>
        </button>`;
    }).join('');
  } catch {
    varsEl.innerHTML = '<p class="text-error">No se pudo cargar el detalle.</p>';
  }
}

function selectVar(vid, element) {
  state.selectedVariante = vid;
  document.querySelectorAll('#md-vars .var-item').forEach(function (x) {
    x.classList.remove('selected');
  });
  element.classList.add('selected');
  document.getElementById('md-add-btn').disabled = false;
}

async function addToCart() {
  if (!Auth.isLoggedIn()) {
    Toast.info('Inicia sesion para agregar al carrito.');
    setTimeout(function () {
      window.location.href = '/pages/auth/login.html';
    }, 600);
    return;
  }

  if (!state.selectedVariante) {
    Toast.info('Selecciona una variante.');
    return;
  }

  const btn = document.getElementById('md-add-btn');
  try {
    btn.disabled = true;
    btn.innerHTML = '<i class="bi bi-arrow-repeat spin"></i> Agregando...';
    await API.post('/api/carrito/items', { varianteId: state.selectedVariante, cantidad: 1 });
    Toast.ok('Producto agregado al carrito.');
    Modal.close('modal-detalle');
  } catch (e) {
    Toast.err(e.message || 'No se pudo agregar al carrito.');
  } finally {
    btn.innerHTML = '<i class="bi bi-cart-plus"></i> Agregar al carrito';
    btn.disabled = false;
  }
}

function bindCatalogEvents() {
  document.getElementById('cat-search')?.addEventListener('input', onSearchInput);

  document.getElementById('cat-filters')?.addEventListener('click', function (event) {
    const btn = event.target.closest('[data-category]');
    if (!btn) return;
    const value = btn.getAttribute('data-category');
    setCategory(value ? Number(value) : null);
  });

  document.getElementById('grid-productos')?.addEventListener('click', function (event) {
    const btn = event.target.closest('[data-detalle]');
    if (!btn) return;
    openDetalle(Number(btn.getAttribute('data-detalle')));
  });

  document.getElementById('pagination')?.addEventListener('click', function (event) {
    const btn = event.target.closest('[data-page]');
    if (!btn) return;
    setPage(Number(btn.getAttribute('data-page')));
  });

  document.getElementById('md-vars')?.addEventListener('click', function (event) {
    const row = event.target.closest('[data-variante]');
    if (!row || row.disabled) return;
    selectVar(Number(row.getAttribute('data-variante')), row);
  });

  document.querySelectorAll('[data-close-modal]')?.forEach(function (btn) {
    btn.addEventListener('click', function () {
      Modal.close(btn.getAttribute('data-close-modal'));
    });
  });

  document.querySelector('[data-add-cart="1"]')?.addEventListener('click', addToCart);
}

document.addEventListener('DOMContentLoaded', async function () {
  bindCatalogEvents();
  await Promise.all([loadPromocionesVigentes(), loadCategorias()]);
  await loadProductos();
});
