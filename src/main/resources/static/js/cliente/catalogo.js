let state = {
  page: 0,
  size: 12,
  categoriaId: null,
  filtroTexto: '',
  totalElements: 0,
  totalPages: 0,
  selectedVariante: null,
};

let productosPagina = [];

async function loadCategorias() {
  try {
    const categorias = await API.get('/api/catalogo/categorias');
    const fc = document.getElementById('cat-filters');
    fc.innerHTML =
      `<button class="btn ${!state.categoriaId ? 'btn-gold' : 'btn-soft'}" onclick="setCategory(null)">Todos</button>` +
      (categorias || [])
        .map((c) => `<button class="btn ${state.categoriaId === c.id ? 'btn-gold' : 'btn-soft'}" onclick="setCategory(${c.id})">${c.nombre}</button>`)
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

async function loadProductos() {
  const grid = document.getElementById('grid-productos');
  grid.innerHTML = Array(4)
    .fill('<div class="col"><div class="card p-3 h-100"><div class="skeleton skeleton-media"></div><div class="skeleton skeleton-title"></div><div class="skeleton skeleton-line"></div><div class="skeleton skeleton-line-short"></div></div></div>')
    .join('');

  let url = `/api/catalogo?page=${state.page}&size=${state.size}`;
  if (state.categoriaId) url += `&categoria=${state.categoriaId}`;

  try {
    const pageData = await API.get(url);
    state.totalElements = pageData.totalElements || 0;
    state.totalPages = pageData.totalPages || 0;
    productosPagina = pageData.content || [];

    const filteredCount = getProductosFiltrados().length;
    document.getElementById('cat-counts').textContent =
      state.filtroTexto ? `${filteredCount} de ${state.totalElements} productos` : `${state.totalElements} productos`;

    renderProductos();
    renderPagination();
  } catch {
    grid.innerHTML = '<div class="empty catalog-error-col"><i class="bi bi-exclamation-triangle"></i><p>Error cargando catálogo.</p></div>';
  }
}

function getProductosFiltrados() {
  const q = state.filtroTexto;
  if (!q) return productosPagina;
  return productosPagina.filter((p) => {
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

  grid.innerHTML = lista
    .map(
      (p) => `
    <div class="col">
      <div class="card card-hover h-100 p-3">
        <div class="product-img-wrapper mb-3">
          <img src="${p.urlFotoPortada || 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=400&q=80'}" class="product-img" alt="${p.nombre}">
        </div>
        <div class="badge ${p.hayStock ? 'badge-green' : 'badge-red'} mb-2">${p.hayStock ? 'Disponible' : 'No disponible'}</div>
        <h3 class="product-title">${p.nombre}</h3>
        <p class="product-desc">${p.descripcion || ''}</p>
        <div class="d-flex justify-content-between align-items-center mt-auto">
          <span class="product-price">${fmt.money(p.precioMinimo)}</span>
          <button class="btn btn-outline-brand btn-sm" onclick="openDetalle(${p.id})"><i class="bi bi-eye"></i> Detalle</button>
        </div>
      </div>
    </div>
  `
    )
    .join('');
}

function renderPagination() {
  const pag = document.getElementById('pagination');
  if (state.totalPages <= 1) {
    pag.innerHTML = '';
    return;
  }

  let html = `<button class="btn btn-outline-brand" ${state.page === 0 ? 'disabled' : ''} onclick="setPage(${state.page - 1})"><i class="bi bi-chevron-left"></i> Anteriores</button>`;
  html += `<span class="pagination-meta">Pág ${state.page + 1} de ${state.totalPages}</span>`;
  html += `<button class="btn btn-outline-brand" ${state.page === state.totalPages - 1 ? 'disabled' : ''} onclick="setPage(${state.page + 1})">Siguientes <i class="bi bi-chevron-right"></i></button>`;
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
    const p = await API.get(`/api/catalogo/${id}`);
    document.getElementById('md-title').textContent = p.nombre;
    document.getElementById('md-img').src = 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=400&q=80';
    document.getElementById('md-desc').textContent = p.descripcion || 'Sin descripción';

    const al = document.getElementById('md-alergenos');
    al.innerHTML = (p.listaAlergenos || []).map((a) => `<span class="badge badge-yellow allergen-badge-xs"><i class="bi bi-exclamation-triangle"></i> ${a}</span>`).join('');

    const vars = p.variantes || [];
    if (!vars.length) {
      varsEl.innerHTML = '<p class="text-xs-muted">No hay variantes disponibles.</p>';
      return;
    }

    varsEl.innerHTML = vars
      .map((v) => {
        const disponible = v.activo && v.stockDisponible > 0;
        return `
      <div class="var-item ${disponible ? '' : 'var-item-disabled'}" id="var-opt-${v.id}" ${disponible ? `onclick="selectVar(${v.id}, this)"` : ''}>
        <span class="var-item-name">${v.nombreVariante} ${disponible ? '' : '(Agotado)'}</span>
        <span class="var-item-price">${fmt.money(v.precio)}</span>
      </div>`;
      })
      .join('');
  } catch {
    varsEl.innerHTML = '<p class="text-error">No se pudo cargar el detalle.</p>';
  }
}

function selectVar(vid, el) {
  state.selectedVariante = vid;
  document.querySelectorAll('.var-item').forEach((x) => x.classList.remove('selected'));
  el.classList.add('selected');
  document.getElementById('md-add-btn').disabled = false;
}

async function addToCart() {
  if (!Auth.isLoggedIn()) {
    Toast.info('Inicia sesión para agregar al carrito');
    setTimeout(() => {
      window.location.href = 'pages/auth/login.html';
    }, 1000);
    return;
  }

  if (!state.selectedVariante) {
    Toast.info('Selecciona una variante');
    return;
  }

  const btn = document.getElementById('md-add-btn');
  try {
    btn.disabled = true;
    btn.innerHTML = '<i class="bi bi-arrow-repeat spin"></i> Agregando...';
    await API.post('/api/carrito/items', { varianteId: state.selectedVariante, cantidad: 1 });
    Toast.ok('Producto agregado al carrito');
    Modal.close('modal-detalle');
  } catch (e) {
    Toast.err(e.message || 'No se pudo agregar al carrito');
  } finally {
    btn.innerHTML = '<i class="bi bi-cart-plus"></i> Agregar al carrito';
    btn.disabled = false;
  }
}

window.setCategory = setCategory;
window.setPage = setPage;
window.openDetalle = openDetalle;
window.selectVar = selectVar;
window.addToCart = addToCart;

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('cat-search')?.addEventListener('input', onSearchInput);
  loadCategorias();
  loadProductos();
});
