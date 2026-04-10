/* ═══════════════════════════════════════════════
   PERSONALIZADOR DE PEDIDOS (RF07)
   ═══════════════════════════════════════════════ */

Auth.requireClient();

const params = new URLSearchParams(window.location.search);
const pedidoId = params.get('pedidoId');
let comprobanteTemp = null;
let detallesFiltrados = []; // Solo los que sean una Torta o producto personalizable. Asumiremos por ahora todos.

document.addEventListener('DOMContentLoaded', () => {
  if (!pedidoId) {
    window.location.href = 'perfil.html';
    return;
  }
  loadDetallesPedido();
});

async function loadDetallesPedido() {
  try {
    const data = await API.get(`/api/pedidos/${pedidoId}/comprobante`);
    comprobanteTemp = data;
    document.getElementById('p-codigo').textContent = data.codigoPedido || `#${data.pedidoId}`;
    
    // Obtenemos los items de este pedido.
    if (!data.items || data.items.length === 0) throw new Error("Pedido vacío");
    
    detallesFiltrados = data.items; // todos los productos adquiridos
    await renderFormularios();
  } catch (err) {
    Toast.err('No se pudo cargar el pedido. ' + err.message);
    document.getElementById('loading-spinner').innerHTML = `<p class="text-danger"><i class="bi bi-x-circle"></i> ${err.message}</p>`;
  }
}

async function renderFormularios() {
  const container = document.getElementById('items-container');
  container.innerHTML = '';
  document.getElementById('loading-spinner').style.display = 'none';

  // Obtenemos las personalizaciones actuales, si existen, desde el backend.
  let perData = [];
  try {
    perData = await API.get(`/api/pedidos/${pedidoId}/personalizacion`);
  } catch (e) {
    console.warn("Sin personalizaciones previas o error obteniendo:", e);
  }

  const html = detallesFiltrados.map((item, index) => {
    // Buscar si existe personalizacion para este detalle (usamos el detalle_pedido_id)
    // El comprobanteDTO mapea el id del array de items como el id del detalle interno si backend fue modificado,
    // OJO: Si comprobante no expone detalle_pedido_id, usaremos el index del array simulando o el id q tenga.
    // Asumiremos que item.detalleId o item.id existe. Si solo expone nombre, vamos a enviar guardados usando el item.id
    const dId = item.detalleId || item.id || (index+1);
    
    const saved = perData.find(p => p.detallePedidoId === dId) || {};

    return `
      <div class="card p-0 shadow-sm pers-card overflow-hidden">
        <div class="card-header bg-dark d-flex justify-content-between align-items-center">
          <h5 class="mb-0 text-white"><i class="bi bi-box-seam me-2"></i>${item.nombre} (x${item.cantidad})</h5>
          <span class="badge bg-danger">Personalización #${index+1}</span>
        </div>
        <div class="card-body bg-card">
          <form id="form-pers-${dId}" onSubmit="event.preventDefault(); guardarPersonalizacion(${dId});">
            
            <div class="row g-4">
              <!-- Columna texto y sabor/textura -->
              <div class="col-md-7">
                <div class="mb-3">
                  <label class="form-label text-secondary fw-semibold">Texto decorativo en el pastel</label>
                  <input type="text" class="form-control bg-input border-secondary text-white" id="p-texto-${dId}" placeholder="Ej: ¡Feliz Aniversario Amor!" value="${saved.textoPastel || ''}" maxlength="60">
                </div>
                
                <div class="row g-3">
                  <div class="col-sm-6">
                    <label class="form-label text-secondary fw-semibold">Sabor del Bizcocho</label>
                    <select class="form-select bg-input border-secondary text-white" id="p-bizcocho-${dId}">
                      <option value="" class="text-dark">Por defecto / Original</option>
                      <option value="Vainilla Macerada" ${saved.saborBizcocho==='Vainilla Macerada'?'selected':''} class="text-dark">Vainilla Macerada</option>
                      <option value="Chocolate Húmedo" ${saved.saborBizcocho==='Chocolate Húmedo'?'selected':''} class="text-dark">Chocolate Húmedo</option>
                      <option value="Red Velvet" ${saved.saborBizcocho==='Red Velvet'?'selected':''} class="text-dark">Red Velvet</option>
                    </select>
                  </div>
                  <div class="col-sm-6">
                    <label class="form-label text-secondary fw-semibold">Tipo de Relleno</label>
                    <select class="form-select bg-input border-secondary text-white" id="p-relleno-${dId}">
                      <option value="" class="text-dark">Por defecto / Original</option>
                      <option value="Manjar Blanco Blanco" ${saved.tipoRelleno==='Manjar Blanco Blanco'?'selected':''} class="text-dark">Manjar Blanco</option>
                      <option value="Crema Chantilly con Fresas" ${saved.tipoRelleno==='Crema Chantilly con Fresas'?'selected':''} class="text-dark">Crema Chantilly con Fresas</option>
                      <option value="Ganache Trufa" ${saved.tipoRelleno==='Ganache Trufa'?'selected':''} class="text-dark">Ganache Trufa</option>
                    </select>
                  </div>
                </div>

                <div class="mt-3">
                  <label class="form-label text-secondary fw-semibold">Notas e indicaciones adicionales del cliente</label>
                  <textarea class="form-control bg-input border-secondary text-white" id="p-notas-${dId}" rows="3" placeholder="Ej: Sin nueces, o decorar con flores rojas a los costados">${saved.notasCliente || ''}</textarea>
                </div>
              </div>

              <!-- Columna visual: Imagen y Colores -->
              <div class="col-md-5">
                <h6 class="text-secondary fw-semibold mb-3">Diseño Visual</h6>
                
                <div class="d-flex align-items-center gap-3 mb-4">
                  <div class="color-picker-wrap">
                    <input type="color" id="p-color-${dId}" value="${saved.colorDecorado || '#CC2020'}">
                  </div>
                  <label class="small text-secondary mb-0">Color de glaseado / decoración principal</label>
                </div>

                <div>
                  <label class="form-label text-secondary fw-semibold">Glosario / Imágen de referencia (Opcional URL)</label>
                  <div class="input-group">
                    <span class="input-group-text bg-dark border-secondary text-white-50"><i class="bi bi-link-45deg"></i></span>
                    <input type="url" class="form-control bg-input border-secondary text-white" id="p-img-${dId}" placeholder="https://..." value="${saved.imagenReferenciaUrl || ''}" oninput="previewImg(${dId})">
                  </div>
                  <img id="p-img-preview-${dId}" src="${saved.imagenReferenciaUrl || ''}" class="img-preview" ${saved.imagenReferenciaUrl ? 'style="display:block;"' : ''}>
                </div>
              </div>

            </div>

            <hr class="border-secondary mt-4 mb-3">
            <div class="d-flex justify-content-end align-items-center">
              <span class="text-success small me-3" id="p-msg-${dId}"></span>
              <button type="submit" class="btn btn-outline-brand px-4 py-2" id="p-btn-${dId}">
                <i class="bi bi-cloud-arrow-up"></i> Guardar preferencias de este item
              </button>
            </div>
          </form>
        </div>
      </div>
    `;
  }).join('');

  container.innerHTML = html;
  document.getElementById('finish-btn-wrap').style.display = 'block';
}

function previewImg(dId) {
  const val = document.getElementById(`p-img-${dId}`).value;
  const imgEL = document.getElementById(`p-img-preview-${dId}`);
  if (val && val.startsWith('http')) {
    imgEL.src = val;
    imgEL.style.display = 'block';
  } else {
    imgEL.style.display = 'none';
    imgEL.src = '';
  }
}

async function guardarPersonalizacion(dId) {
  const btn = document.getElementById(`p-btn-${dId}`);
  const msg = document.getElementById(`p-msg-${dId}`);
  
  const payload = {
    saborBizcocho: document.getElementById(`p-bizcocho-${dId}`).value || null,
    tipoRelleno: document.getElementById(`p-relleno-${dId}`).value || null,
    colorDecorado: document.getElementById(`p-color-${dId}`).value || null,
    textoPastel: document.getElementById(`p-texto-${dId}`).value || null,
    notasCliente: document.getElementById(`p-notas-${dId}`).value || null,
    imagenReferenciaUrl: document.getElementById(`p-img-${dId}`).value || null
  };

  try {
    btn.disabled = true;
    btn.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span>Guardando...`;
    
    // Llamada API
    await API.post(`/api/pedidos/${pedidoId}/personalizacion/items/${dId}`, payload);
    
    Toast.ok('¡Diseño guardado exitosamente!');
    msg.innerHTML = '<i class="bi bi-check-circle-fill"></i> Guardado';
    btn.classList.remove('btn-outline-brand');
    btn.classList.add('btn-success');
    setTimeout(() => {
      msg.innerHTML = '';
      btn.classList.remove('btn-success');
      btn.classList.add('btn-outline-brand');
      btn.innerHTML = '<i class="bi bi-cloud-arrow-up"></i> Actualizar preferencias';
      btn.disabled = false;
    }, 2000);
  } catch (err) {
    Toast.err(err.message || 'Error al guardar personalización');
    btn.disabled = false;
    btn.innerHTML = '<i class="bi bi-cloud-arrow-up"></i> Reintentar guardado';
  }
}
