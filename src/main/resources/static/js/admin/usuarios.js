Auth.requireAdmin();

const btnNuevo = document.getElementById('btn-nuevo-empleado');
const btnClose = document.getElementById('btn-close-empleado');
const form = document.getElementById('form-empleado');
const submitBtn = document.getElementById('emp-submit');

if (btnNuevo) {
  btnNuevo.addEventListener('click', () => {
    Modal.open('modal-empleado');
  });
}

if (btnClose) {
  btnClose.addEventListener('click', () => {
    Modal.close('modal-empleado');
  });
}

function normalizarTelefono(v) {
  return String(v || '').replace(/\D/g, '').slice(0, 9);
}

async function crearEmpleado(ev) {
  ev.preventDefault();

  const payload = {
    nombre: document.getElementById('emp-nombre').value.trim(),
    email: document.getElementById('emp-email').value.trim(),
    password: document.getElementById('emp-password').value,
    telefono: normalizarTelefono(document.getElementById('emp-telefono').value),
    rol: document.getElementById('emp-rol').value,
  };

  if (!payload.nombre || !payload.email || !payload.password || !payload.rol) {
    Toast.err('Completa los campos obligatorios');
    return;
  }
  if (payload.password.length < 6) {
    Toast.err('La contraseña debe tener al menos 6 caracteres');
    return;
  }

  if (!payload.telefono) delete payload.telefono;

  try {
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="bi bi-arrow-repeat spin"></i> Creando...';

    const res = await API.post('/api/admin/usuarios/empleados', payload);
    const data = res?.data || res;

    Toast.ok(`Empleado creado: ${data?.email || payload.email}`);
    form.reset();
    Modal.close('modal-empleado');

    setTimeout(() => window.location.reload(), 400);
  } catch (err) {
    Toast.err(err.message || 'No se pudo crear el empleado');
  } finally {
    submitBtn.disabled = false;
    submitBtn.innerHTML = '<i class="bi bi-save"></i> Crear empleado';
  }
}

if (form) {
  form.addEventListener('submit', crearEmpleado);
}
