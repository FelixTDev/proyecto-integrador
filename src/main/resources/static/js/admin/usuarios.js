Auth.requireAdmin();
Auth.requireRole('ROLE_ADMIN');

const btnNuevo = document.getElementById('btn-nuevo-empleado');
const formEmpleado = document.getElementById('form-empleado');
const submitEmpleado = document.getElementById('emp-submit');
const formEstado = document.getElementById('form-estado-usuario');
const submitEstado = document.getElementById('usr-accion-submit');
const opsBody = document.getElementById('ops-body');

const ops = [];

function addOp(tipo, usuario, resultado) {
  ops.unshift({
    fecha: new Date().toISOString(),
    tipo,
    usuario,
    resultado
  });
  renderOps();
}

function renderOps() {
  if (!ops.length) {
    opsBody.innerHTML = '<tr><td colspan="4" class="text-center text-secondary py-4">Aun no hay operaciones ejecutadas.</td></tr>';
    return;
  }

  opsBody.innerHTML = ops.map(function (op) {
    return `
      <tr>
        <td>${fmt.dt(op.fecha)}</td>
        <td>${op.tipo}</td>
        <td>${op.usuario}</td>
        <td><span class="badge badge-green">${op.resultado}</span></td>
      </tr>`;
  }).join('');
}

function normalizarTelefono(v) {
  return String(v || '').replace(/\D/g, '').slice(0, 9);
}

async function crearEmpleado(ev) {
  ev.preventDefault();

  const payload = {
    nombre: document.getElementById('emp-nombre').value.trim(),
    email: document.getElementById('emp-email').value.trim().toLowerCase(),
    password: document.getElementById('emp-password').value,
    telefono: normalizarTelefono(document.getElementById('emp-telefono').value),
    rol: document.getElementById('emp-rol').value
  };

  if (!payload.nombre || !payload.email || !payload.password || !payload.rol) {
    Toast.err('Completa los campos obligatorios.');
    return;
  }
  if (payload.password.length < 6) {
    Toast.err('La contrasena debe tener al menos 6 caracteres.');
    return;
  }

  if (!payload.telefono) delete payload.telefono;

  try {
    submitEmpleado.disabled = true;
    submitEmpleado.innerHTML = '<i class="bi bi-arrow-repeat spin"></i> Creando...';

    const data = await API.post('/api/admin/usuarios/empleados', payload);

    Toast.ok('Empleado creado con ID ' + data.id);
    addOp('Crear empleado', data.email || payload.email, 'ID ' + data.id);
    formEmpleado.reset();
    Modal.close('modal-empleado');
  } catch (err) {
    Toast.err(err.message || 'No se pudo crear el empleado');
  } finally {
    submitEmpleado.disabled = false;
    submitEmpleado.innerHTML = '<i class="bi bi-save"></i> Crear empleado';
  }
}

async function cambiarEstadoUsuario(ev) {
  ev.preventDefault();
  const id = Number(document.getElementById('usr-id').value);
  const accion = document.getElementById('usr-accion').value;

  if (!Number.isFinite(id) || id < 1) {
    Toast.err('ID de usuario invalido.');
    return;
  }

  const endpoint = accion === 'activar'
    ? '/api/admin/usuarios/' + id + '/activar'
    : '/api/admin/usuarios/' + id + '/desactivar';

  try {
    submitEstado.disabled = true;
    submitEstado.innerHTML = '<i class="bi bi-arrow-repeat spin"></i> Ejecutando...';

    await API.patch(endpoint, {});

    const resultado = accion === 'activar' ? 'Activado' : 'Desactivado';
    Toast.ok('Usuario ' + resultado.toLowerCase() + ' correctamente.');
    addOp('Cambio de estado', 'Usuario #' + id, resultado);
    formEstado.reset();
  } catch (err) {
    Toast.err(err.message || 'No se pudo actualizar el estado del usuario');
  } finally {
    submitEstado.disabled = false;
    submitEstado.innerHTML = '<i class="bi bi-check2-circle"></i> Ejecutar accion';
  }
}

document.addEventListener('DOMContentLoaded', () => {
  renderOps();
  btnNuevo.addEventListener('click', () => Modal.open('modal-empleado'));
  formEmpleado.addEventListener('submit', crearEmpleado);
  formEstado.addEventListener('submit', cambiarEstadoUsuario);
});
