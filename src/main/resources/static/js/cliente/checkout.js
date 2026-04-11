Auth.requireClient();

const state = {
  esRecojo: false,
  cartId: null,
  cotizacion: null,
  direcciones: [],
  direccionSeleccionadaId: null,
  cuponAplicado: null,
  franjas: []
};

function qs(id) {
  return document.getElementById(id);
}

function getFranjaId() {
  const value = Number(qs('f-hora').value);
  return Number.isFinite(value) ? value : null;
}

function getDireccionId() {
  return state.esRecojo ? null : state.direccionSeleccionadaId;
}

function buildPaymentToken() {
  const metodo = Number(document.querySelector('input[name="pago"]:checked')?.value || 1);
  if (metodo !== 1) {
    return 'wallet_' + Date.now();
  }

  const num = (qs('card-num').value || '').replace(/\D/g, '');
  const exp = (qs('card-exp').value || '').trim();
  const cvv = (qs('card-cvv').value || '').trim();

  if (num.length < 13 || exp.length !== 5 || cvv.length < 3) {
    throw new Error('Completa los datos de tarjeta para continuar.');
  }

  return 'card_' + num.slice(-4) + '_' + exp.replace('/', '') + '_' + Date.now();
}

function normalizeCardInputs() {
  qs('card-num').addEventListener('input', () => {
    const digits = qs('card-num').value.replace(/\D/g, '').slice(0, 16);
    qs('card-num').value = digits.replace(/(.{4})/g, '$1 ').trim();
  });

  qs('card-exp').addEventListener('input', () => {
    const digits = qs('card-exp').value.replace(/\D/g, '').slice(0, 4);
    qs('card-exp').value = digits.length > 2 ? digits.slice(0, 2) + '/' + digits.slice(2) : digits;
  });

  qs('card-cvv').addEventListener('input', () => {
    qs('card-cvv').value = qs('card-cvv').value.replace(/\D/g, '').slice(0, 4);
  });
}

function renderResumenItems(items) {
  qs('sum-items').innerHTML = items.map((i) => {
    return `<div class="summary-item-row"><span>${i.cantidad}x ${i.productoNombre}</span><span class="summary-item-price">${fmt.money(i.subtotal)}</span></div>`;
  }).join('');
}

function renderDirecciones() {
  const list = qs('addr-list');
  const msg = qs('addr-msg');
  if (!state.direcciones.length) {
    list.innerHTML = '<div class="card empty-card-note"><i class="bi bi-geo-alt"></i> No tienes direcciones activas registradas.</div>';
    msg.textContent = 'Debes registrar al menos una direccion activa para delivery.';
    return;
  }

  list.innerHTML = state.direcciones.map((d) => {
    const checked = d.id === state.direccionSeleccionadaId ? 'checked' : '';
    const zona = d.zonaNombre ? ` - ${d.zonaNombre}` : '';
    return `
      <label class="pay-opt address-option-start">
        <input type="radio" name="addr" value="${d.id}" ${checked}>
        <div class="address-content-grow">
          <h4 class="address-title">${d.etiqueta || 'Direccion'} ${d.esPrincipal ? '<span class="badge badge-gold badge-inline-gap">Principal</span>' : ''}</h4>
          <p class="address-line">${d.direccionCompleta}${zona}</p>
          <p class="address-meta">${d.destinatarioNombre || ''} ${d.destinatarioTelefono || ''}</p>
        </div>
      </label>`;
  }).join('');

  list.querySelectorAll('input[name="addr"]').forEach((el) => {
    el.addEventListener('change', (ev) => {
      state.direccionSeleccionadaId = Number(ev.target.value);
      recotizar();
    });
  });

  msg.textContent = 'Se usara la direccion seleccionada para cotizar envio y procesar el pedido.';
}

function franjasSegunModo() {
  return state.franjas.filter((f) => {
    if (state.esRecojo) return f.tipo === 'RECOJO' || f.tipo === 'AMBOS';
    return f.tipo === 'DELIVERY' || f.tipo === 'AMBOS';
  });
}

function renderFranjas() {
  const sel = qs('f-hora');
  const opciones = franjasSegunModo();

  if (!opciones.length) {
    sel.innerHTML = '<option value="">Sin franjas disponibles</option>';
    return;
  }

  sel.innerHTML = opciones.map((f) => {
    const disponibilidad = f.disponible ? `(${f.cuposDisponibles} cupos)` : '(Agotado)';
    return `<option value="${f.id}" ${f.disponible ? '' : 'disabled'}>${f.fecha} ${f.horaInicio}-${f.horaFin} ${disponibilidad}</option>`;
  }).join('');
}

async function loadFranjas() {
  const hoy = new Date().toISOString().split('T')[0];
  const manana = new Date(Date.now() + 86400000).toISOString().split('T')[0];

  const [fh, fm] = await Promise.all([
    API.get('/api/entregas/franjas?fecha=' + hoy, { auth: false, retryOn401: false }).catch(() => []),
    API.get('/api/entregas/franjas?fecha=' + manana, { auth: false, retryOn401: false }).catch(() => [])
  ]);

  state.franjas = [...(fh || []), ...(fm || [])];
  renderFranjas();
}

async function validarCupon() {
  const code = qs('input-cupon').value.trim();
  if (!code) {
    Toast.info('Ingresa un codigo de cupon.');
    return;
  }

  try {
    const result = await API.post('/api/promociones/validar-cupon', {
      codigoCupon: code,
      subtotal: state.cotizacion?.subtotal || 0
    });

    if (!result.aplicado) {
      state.cuponAplicado = null;
      qs('cupon-badge').innerHTML = '';
      Toast.err(result.mensaje || 'Cupon no aplicable.');
      return;
    }

    state.cuponAplicado = result;
    qs('cupon-badge').innerHTML = `<span class="badge bg-success"><i class="bi bi-check"></i> ${result.nombrePromocion} (-${fmt.money(result.descuento)})</span>`;
    Toast.ok(result.mensaje || 'Cupon aplicado.');
  } catch (error) {
    state.cuponAplicado = null;
    qs('cupon-badge').innerHTML = '';
    Toast.err(error.message || 'No se pudo validar el cupon.');
  }
}

async function recotizar() {
  if (!state.cartId) return;

  try {
    state.cotizacion = await API.post('/api/pagos/cotizacion', {
      carritoId: state.cartId,
      direccionId: getDireccionId(),
      esRecojoTienda: state.esRecojo,
      franjaHorariaId: getFranjaId(),
      zonaEntrega: null
    });
  } catch (error) {
    state.cotizacion = null;
    Toast.err(error.message || 'No se pudo cotizar el pedido.');
  }

  renderTotales();
}

function renderTotales() {
  const c = state.cotizacion;
  const btn = qs('btn-pay');

  if (!c) {
    qs('sum-sub').textContent = fmt.money(0);
    qs('sum-igv').textContent = fmt.money(0);
    qs('sum-envio').textContent = fmt.money(0);
    qs('sum-tot').textContent = fmt.money(0);
    btn.disabled = true;
    return;
  }

  qs('sum-sub').textContent = fmt.money(c.subtotal);
  qs('sum-igv').textContent = fmt.money(c.impuestos);
  qs('sum-envio').textContent = fmt.money(c.costoEnvio);
  qs('sum-tot').textContent = fmt.money(c.total);
  qs('row-envio').style.display = state.esRecojo ? 'none' : 'flex';

  const franjaMsg = qs('franja-msg');
  franjaMsg.textContent = c.mensajeFranja || 'Franja validada';
  franjaMsg.style.color = c.franjaDisponible ? 'var(--green)' : 'var(--red)';

  const sinDireccion = !state.esRecojo && !state.direccionSeleccionadaId;
  btn.disabled = !c.franjaDisponible || sinDireccion;
}

function toggleDeliveryMode(mode) {
  state.esRecojo = mode === 'pickup';
  document.querySelectorAll('[data-delivery-mode]').forEach((btn) => {
    btn.classList.toggle('active', btn.getAttribute('data-delivery-mode') === mode);
  });
  qs('form-delivery').style.display = state.esRecojo ? 'none' : 'block';
  renderFranjas();
  recotizar();
}

function validateProcess() {
  if (!state.cotizacion) return 'No hay cotizacion disponible.';
  if (!state.cotizacion.franjaDisponible) return 'La franja seleccionada no tiene cupo.';
  if (!state.esRecojo && !state.direccionSeleccionadaId) return 'Debes seleccionar una direccion de entrega.';
  return null;
}

function renderConfirmacion(result) {
  qs('modal-ok-content').innerHTML = `
    <div class="mx-auto mb-3 d-flex align-items-center justify-content-center rounded-circle modal-success-icon"><i class="bi bi-check-lg"></i></div>
    <h2 class="h3 mb-2">Pedido confirmado</h2>
    <p class="text-secondary mb-3">${result.mensaje || 'Pago aprobado correctamente.'}</p>
    <p class="small text-secondary mb-4">Pedido #${result.pedidoId} - Referencia ${result.referenciaExterna || 'N/A'}</p>
    <a href="personalizar.html?pedidoId=${result.pedidoId}" class="btn btn-brand w-100 mb-2"><i class="bi bi-magic"></i> Personalizar pedido</a>
    <a href="perfil.html#tab-historial" class="btn btn-outline-brand w-100 mb-2">Ver historial</a>
    <a href="../../index.html" class="btn btn-outline-secondary w-100">Volver al inicio</a>`;
  Modal.open('modal-ok');
}

async function doProcess() {
  const error = validateProcess();
  if (error) {
    Toast.err(error);
    return;
  }

  const btn = qs('btn-pay');
  btn.disabled = true;
  btn.innerHTML = '<i class="bi bi-arrow-repeat spin"></i> Procesando pago...';

  try {
    const metodoPagoId = Number(document.querySelector('input[name="pago"]:checked').value);
    const tokenTarjeta = buildPaymentToken();

    const result = await API.post('/api/pagos/procesar', {
      carritoId: state.cartId,
      email: Auth.email(),
      esRecojoTienda: state.esRecojo,
      direccionId: getDireccionId(),
      zonaEntrega: null,
      franjaHorariaId: getFranjaId(),
      metodoPagoId,
      tokenTarjeta
    });

    if (!result.aprobado) {
      Toast.err(result.mensaje || 'Pago rechazado');
      btn.disabled = false;
      btn.innerHTML = '<i class="bi bi-lock-fill"></i> Completar pago';
      return;
    }

    renderConfirmacion(result);
  } catch (e) {
    Toast.err(e.message || 'Error en el procesamiento del pago');
    btn.disabled = false;
    btn.innerHTML = '<i class="bi bi-lock-fill"></i> Completar pago';
  }
}

function bindEvents() {
  document.querySelectorAll('[data-delivery-mode]').forEach((el) => {
    el.addEventListener('click', () => toggleDeliveryMode(el.getAttribute('data-delivery-mode')));
  });

  document.querySelectorAll('input[name="pago"]').forEach((r) => {
    r.addEventListener('change', (e) => {
      qs('card-form-wrap').style.display = e.target.value === '1' ? 'block' : 'none';
    });
  });

  qs('f-hora').addEventListener('change', recotizar);
  qs('btn-cupon').addEventListener('click', validarCupon);
  qs('btn-pay').addEventListener('click', doProcess);
  normalizeCardInputs();
}

async function initCheckout() {
  bindEvents();

  try {
    const [carrito, direcciones] = await Promise.all([
      API.get('/api/carrito'),
      API.get('/api/cliente/direcciones')
    ]);

    if (!carrito.items || !carrito.items.length) {
      window.location.href = 'carrito.html';
      return;
    }

    state.cartId = carrito.id;
    state.direcciones = direcciones || [];

    const principal = state.direcciones.find((d) => d.esPrincipal) || state.direcciones[0] || null;
    state.direccionSeleccionadaId = principal ? principal.id : null;

    renderResumenItems(carrito.items);
    renderDirecciones();
    await loadFranjas();
    await recotizar();
  } catch (e) {
    Toast.err(e.message || 'Error al cargar checkout');
  }
}

document.addEventListener('DOMContentLoaded', initCheckout);
