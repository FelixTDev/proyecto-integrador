Auth.requireClient();

let cartData = null;

async function loadCart() {
  const root = document.getElementById('cart-content');
  try {
    cartData = await API.get('/api/carrito');
    renderCart(root);
  } catch (e) {
    root.innerHTML = `<div class="empty"><i class="bi bi-exclamation-triangle"></i><p>No se pudo cargar tu carrito. ${e.message}</p></div>`;
  }
}

function renderCart(root) {
  if (!cartData || !cartData.items || cartData.items.length === 0) {
    root.innerHTML = `
      <div class="empty">
        <i class="bi bi-cart-x"></i>
        <h3 style="color:var(--brand-primary);font-family:var(--font-display);font-size:1.8rem;margin-bottom:0.5rem">Tu carrito está vacío</h3>
        <p style="margin-bottom:1.5rem">Anímate a explorar nuestras delicias frescas.</p>
        <a href="../../index.html" class="btn btn-brand">Ver Catálogo</a>
      </div>
    `;
    return;
  }

  const itemsHtml = cartData.items
    .map(
      (item) => `
      <div class="cart-item">
        <img src="${item.imagenUrl || 'https://images.unsplash.com/photo-1542826438-bd32f43d626f?w=200&q=80'}" class="ci-img" alt="${item.productoNombre}">
        <div class="ci-details">
          <div>
            <div class="flex-between">
              <h4 class="ci-title">${item.productoNombre}</h4>
              <button class="btn btn-soft btn-sm" style="color:#9f2323;padding:0.2rem" onclick="removeItem(${item.detalleId})" title="Eliminar"><i class="bi bi-trash3"></i></button>
            </div>
            <p class="ci-var">Variante: ${item.varianteNombre}</p>
            <p style="font-size:0.75rem;color:#857272">Stock disponible: ${item.stockDisponible}</p>
          </div>
          <div class="ci-controls">
            <div class="qty-ctl">
              <button class="qty-btn" onclick="updateQty(${item.detalleId}, ${item.cantidad - 1})"><i class="bi bi-dash"></i></button>
              <input type="text" class="qty-val" value="${item.cantidad}" readonly>
              <button class="qty-btn" onclick="updateQty(${item.detalleId}, ${item.cantidad + 1})"><i class="bi bi-plus"></i></button>
            </div>
            <div class="ci-price">${fmt.money(item.subtotal)}</div>
          </div>
        </div>
      </div>
    `
    )
    .join('');

  root.innerHTML = `
    <div class="cart-grid">
      <div class="cart-items">
        <div class="flex-between" style="margin-bottom:1.25rem;border-bottom:1px solid var(--border);padding-bottom:1rem">
          <h3 style="color:var(--brand-primary);font-family:var(--font-display);font-size:1.4rem">Tus productos</h3>
          <button class="btn btn-outline-brand btn-sm" onclick="clearCart()"><i class="bi bi-trash2"></i> Vaciar todo</button>
        </div>
        ${itemsHtml}
      </div>
      <div class="cart-summary">
        <h3 style="color:var(--brand-primary);font-family:var(--font-display);font-size:1.4rem;margin-bottom:1.5rem">Resumen del pedido</h3>
        <div class="sum-row"><span>Subtotal</span><span>${fmt.money(cartData.subtotal)}</span></div>
        <div class="sum-row"><span>IGV (18%)</span><span>${fmt.money(cartData.igv)}</span></div>
        <div class="sum-row"><span>Envío</span><span>Se calcula en checkout</span></div>
        <div class="sum-total"><span>Total (sin envío)</span><span>${fmt.money(cartData.total)}</span></div>
        
        <div class="divider"></div>
        <a href="checkout.html" class="btn btn-brand btn-full btn-lg">Proceder al pago <i class="bi bi-credit-card"></i></a>
        <p style="text-align:center;font-size:0.75rem;color:#7a6767;margin-top:1rem"><i class="bi bi-shield-check" style="color:#2f8656"></i> Pago seguro y encriptado</p>
      </div>
    </div>
  `;
}

async function updateQty(id, qty) {
  if (qty < 1) return removeItem(id);
  try {
    await API.put(`/api/carrito/items/${id}?cantidad=${qty}`);
    loadCart();
  } catch (e) {
    Toast.err(`No se pudo actualizar cantidad: ${e.message}`);
  }
}

async function removeItem(id) {
  if (!confirm('¿Quitar este producto del carrito?')) return;
  try {
    await API.delete(`/api/carrito/items/${id}`);
    Toast.info('Producto retirado');
    loadCart();
  } catch (e) {
    Toast.err(e.message || 'No se pudo retirar el producto');
  }
}

async function clearCart() {
  if (!confirm('¿Estás seguro de vaciar todo el carrito?')) return;
  try {
    await API.delete('/api/carrito');
    loadCart();
  } catch (e) {
    Toast.err(e.message || 'No se pudo vaciar el carrito');
  }
}

window.updateQty = updateQty;
window.removeItem = removeItem;
window.clearCart = clearCart;

document.addEventListener('DOMContentLoaded', loadCart);
