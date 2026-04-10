Auth.requireClient();

let esRecojo = false;
let cartId = null;
let cotizacionActual = null;
let direccionesActivas = [];
let direccionSeleccionadaId = null;
let cuponAplicado = null;

function getFranjaId() {
  return parseInt(document.getElementById('f-hora').value, 10);
}

function getDireccionSeleccionadaId() {
  if (esRecojo) return null;
  return direccionSeleccionadaId;
}

async function initCheckout() {
  try {
    const [carrito, direcciones] = await Promise.all([
      API.get('/api/carrito'),
      API.get('/api/cliente/direcciones'),
    ]);

    if (!carrito.items || !carrito.items.length) {
      window.location.href = 'carrito.html';
      return;
    }

    direccionesActivas = direcciones || [];
    const principal = direccionesActivas.find((d) => d.esPrincipal) || direccionesActivas[0] || null;
    direccionSeleccionadaId = principal ? principal.id : null;

    cartId = carrito.id;
    renderResumenItems(carrito.items);
    renderDirecciones();
    loadFranjas();
    await recotizar();

    document.querySelectorAll('input[name="pago"]').forEach((r) =>
      r.addEventListener('change', (e) => {
        document.getElementById('card-mock').style.display = e.target.value === '1' ? 'block' : 'none';
      })
    );

    // Coupon handler
    const cuponBtn = document.getElementById('btn-cupon');
    if (cuponBtn) cuponBtn.addEventListener('click', validarCupon);
  } catch (e) {
    Toast.err(e.message || 'Error al cargar checkout');
  }
}

async function loadFranjas() {
  const sel = document.getElementById('f-hora');
  if (!sel) return;
  try {
    const hoy = new Date().toISOString().split('T')[0];
    const manana = new Date(Date.now() + 86400000).toISOString().split('T')[0];
    const [franjasHoy, franjasManana] = await Promise.all([
      API.get(`/api/entregas/franjas?fecha=${hoy}`).catch(() => []),
      API.get(`/api/entregas/franjas?fecha=${manana}`).catch(() => [])
    ]);
    const todas = [...(franjasHoy||[]), ...(franjasManana||[])];
    if (todas.length === 0) {
      sel.innerHTML = '<option value="1">09:00 - 12:00 (Mañana)</option><option value="2">14:00 - 17:00 (Tarde)</option><option value="3">17:00 - 20:00 (Noche)</option>';
      return;
    }
    sel.innerHTML = todas.map(f => {
      const disp = f.disponible ? `(${f.cuposDisponibles} cupos)` : '(Agotado)';
      return `<option value="${f.id}" ${!f.disponible?'disabled':''}>${f.fecha} ${f.horaInicio}-${f.horaFin} ${disp}</option>`;
    }).join('');
  } catch {
    sel.innerHTML = '<option value="1">09:00 - 12:00</option><option value="2">14:00 - 17:00</option>';
  }
}

async function validarCupon() {
  const input = document.getElementById('input-cupon');
  if (!input || !input.value.trim()) { Toast.info('Ingresa un código de cupón'); return; }
  try {
    const result = await API.post('/api/promociones/validar-cupon', {
      codigoCupon: input.value.trim(),
      subtotal: cotizacionActual?.subtotal || 0
    });
    if (result.aplicado) {
      cuponAplicado = result;
      Toast.ok(result.mensaje);
      const badge = document.getElementById('cupon-badge');
      if (badge) badge.innerHTML = `<span class="badge bg-success"><i class="bi bi-check"></i> ${result.nombrePromocion} (-${fmt.money(result.descuento)})</span>`;
    } else {
      cuponAplicado = null;
      Toast.err(result.mensaje);
    }
  } catch { cuponAplicado = null; }
}

function renderResumenItems(items) {
  document.getElementById('sum-items').innerHTML = items
    .map(
      (i) =>
        `<div class="summary-item-row"><span>${i.cantidad}x ${i.productoNombre}</span><span class="summary-item-price">${fmt.money(i.subtotal)}</span></div>`
    )
    .join('');
}

function renderDirecciones() {
  const list = document.getElementById('addr-list');
  const msg = document.getElementById('addr-msg');
  if (!list) return;

  if (!direccionesActivas.length) {
    list.innerHTML = '<div class="card empty-card-note"><i class="bi bi-geo-alt"></i> No tienes direcciones activas registradas.</div>';
    if (msg) msg.textContent = 'Debes registrar al menos una dirección activa para delivery.';
    return;
  }

  list.innerHTML = direccionesActivas
    .map((d) => {
      const checked = d.id === direccionSeleccionadaId ? 'checked' : '';
      const zona = d.zonaNombre ? ` - ${d.zonaNombre}` : '';
      return `
        <label class="pay-opt address-option-start">
          <input type="radio" name="addr" value="${d.id}" ${checked}>
          <div class="address-content-grow">
            <h4 class="address-title">${d.etiqueta || 'Dirección'} ${d.esPrincipal ? '<span class="badge badge-gold badge-inline-gap">Principal</span>' : ''}</h4>
            <p class="address-line">${d.direccionCompleta}${zona}</p>
            <p class="address-meta">${d.destinatarioNombre || ''} ${d.destinatarioTelefono || ''}</p>
          </div>
        </label>`;
    })
    .join('');

  list.querySelectorAll('input[name="addr"]').forEach((el) => {
    el.addEventListener('change', (ev) => {
      direccionSeleccionadaId = parseInt(ev.target.value, 10);
      recotizar();
    });
  });

  if (msg) msg.textContent = 'Se usará la dirección seleccionada para cotizar envío y procesar el pedido.';
}

async function recotizar() {
  if (!cartId) return;
  try {
    cotizacionActual = await API.post('/api/pagos/cotizacion', {
      carritoId: cartId,
      direccionId: getDireccionSeleccionadaId(),
      esRecojoTienda: esRecojo,
      franjaHorariaId: getFranjaId(),
      zonaEntrega: null,
    });
    updateResumenTotales();
  } catch (e) {
    cotizacionActual = null;
    updateResumenTotales();
    Toast.err(e.message || 'No se pudo cotizar el pedido');
  }
}

function updateResumenTotales() {
  const btn = document.getElementById('btn-pay');
  if (!cotizacionActual) {
    document.getElementById('sum-sub').textContent = fmt.money(0);
    document.getElementById('sum-igv').textContent = fmt.money(0);
    document.getElementById('sum-envio').textContent = fmt.money(0);
    document.getElementById('sum-tot').textContent = fmt.money(0);
    if (btn) btn.disabled = true;
    return;
  }

  document.getElementById('sum-sub').textContent = fmt.money(cotizacionActual.subtotal);
  document.getElementById('sum-igv').textContent = fmt.money(cotizacionActual.impuestos);
  document.getElementById('sum-envio').textContent = fmt.money(cotizacionActual.costoEnvio);
  document.getElementById('sum-tot').textContent = fmt.money(cotizacionActual.total);
  document.getElementById('row-envio').style.display = esRecojo ? 'none' : 'flex';

  const msg = document.getElementById('franja-msg');
  if (msg) {
    msg.textContent = cotizacionActual.mensajeFranja || '';
    msg.style.color = cotizacionActual.franjaDisponible ? 'var(--green)' : 'var(--red)';
  }

  const sinDireccion = !esRecojo && !direccionSeleccionadaId;
  if (btn) btn.disabled = !cotizacionActual.franjaDisponible || sinDireccion;
}

function setDelivery(val, el) {
  esRecojo = val;
  document.querySelectorAll('.dlv-card').forEach((x) => x.classList.remove('active'));
  el.classList.add('active');
  document.getElementById('form-delivery').style.display = val ? 'none' : 'block';
  recotizar();
}

function validarFormulario() {
  if (!cotizacionActual) return 'No se pudo calcular el pedido';
  if (!cotizacionActual.franjaDisponible) return 'La franja seleccionada no tiene cupo disponible';
  if (!esRecojo && !direccionSeleccionadaId) return 'Debes seleccionar una dirección de entrega';
  return null;
}

async function doProcess() {
  const error = validarFormulario();
  if (error) {
    Toast.err(error);
    return;
  }

  const btn = document.getElementById('btn-pay');
  try {
    btn.disabled = true;
    btn.innerHTML = '<i class="bi bi-arrow-repeat spin"></i> Procesando pago...';

    const metodoPagoId = parseInt(document.querySelector('input[name="pago"]:checked').value, 10);
    const tokenTarjeta = metodoPagoId === 1 ? 'tok_mock_card_visa' : 'tok_mock_wallet';

    const result = await API.post('/api/pagos/procesar', {
      carritoId: cartId,
      email: Auth.email(),
      esRecojoTienda: esRecojo,
      direccionId: getDireccionSeleccionadaId(),
      zonaEntrega: null,
      franjaHorariaId: getFranjaId(),
      metodoPagoId,
      tokenTarjeta,
    });

    if (result.aprobado) {
      document.getElementById('modal-ok').innerHTML = `
        <div class="modal text-center modal-success">
          <div class="mx-auto mb-3 d-flex align-items-center justify-content-center rounded-circle modal-success-icon"><i class="bi bi-check-lg"></i></div>
          <h2 class="h3 mb-2">¡Pedido confirmado!</h2>
          <p class="text-secondary mb-4">Enviamos el comprobante a tu correo. ¿Quieres añadir diseño, colores o un mensaje especial a tu torta?</p>
          <a href="personalizar.html?pedidoId=${result.pedidoId}" class="btn btn-brand w-100 mb-2"><i class="bi bi-magic"></i> ¡Sí, personalizar diseño!</a>
          <a href="../../index.html" class="btn btn-outline-secondary w-100">No gracias, volver al inicio</a>
        </div>
      `;
      Modal.open('modal-ok');
      return;
    }

    Toast.err(result.mensaje || 'Pago rechazado');
    btn.disabled = false;
    btn.innerHTML = '<i class="bi bi-lock-fill"></i> Intentar nuevamente';
  } catch (e) {
    Toast.err(e.message || 'Error en el procesamiento del pago');
    btn.disabled = false;
    btn.innerHTML = '<i class="bi bi-lock-fill"></i> Intentar nuevamente';
  }
}

window.setDelivery = setDelivery;
window.doProcess = doProcess;

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('f-hora')?.addEventListener('change', recotizar);
  initCheckout();
});
