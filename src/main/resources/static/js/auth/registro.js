if (Auth.isLoggedIn()) {
  window.location.href = '../../index.html';
}

const regForm = document.getElementById('regForm');
const btnSubmit = document.getElementById('btn-submit');
const telInput = document.getElementById('f-tel');
const nombreInput = document.getElementById('f-nombre');

function validarRegistro({ nombre, email, pwd, tel }) {
  if (!nombre) return 'Ingresa tus nombres completos.';
  if (/\d/.test(nombre)) return 'El nombre no debe contener números.';
  if (!/^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ\s]+$/.test(nombre)) {
    return 'El nombre solo debe contener letras y espacios.';
  }

  if (!email) return 'Ingresa tu correo electrónico.';

  if (!pwd || pwd.length < 6) {
    return 'La contraseña debe tener al menos 6 caracteres.';
  }

  if (tel && !/^\d{9}$/.test(tel)) {
    return 'El número de celular debe tener 9 dígitos.';
  }

  return null;
}

async function doRegistro(e) {
  e.preventDefault();

  const nombre = document.getElementById('f-nombre').value.trim();
  const email = document.getElementById('f-email').value.trim();
  const pwd = document.getElementById('f-pwd').value.trim();
  const tel = telInput.value.trim();

  const error = validarRegistro({ nombre, email, pwd, tel });
  if (error) {
    Toast.err(error);
    return;
  }

  try {
    btnSubmit.disabled = true;
    btnSubmit.innerHTML = '<i class="bi bi-arrow-repeat spin"></i> Registrando...';

    const res = await API.post('/api/auth/registro', {
      nombre,
      email,
      password: pwd,
      telefono: tel || null,
    });

    const token = typeof res === 'string' ? res : (res.data?.token || res.token);
    if (token && token.length > 20) {
      Auth.save(token, email);
      Toast.ok('¡Tu cuenta ha sido creada exitosamente!');
      setTimeout(() => {
        window.location.href = '../../index.html';
      }, 1000);
      return;
    }

    throw new Error('Token no recibido');
  } catch (err) {
    Toast.err('Error en el registro. Quizá el correo ya está en uso.');
  } finally {
    btnSubmit.disabled = false;
    btnSubmit.innerHTML = 'Crear mi cuenta';
  }
}

if (telInput) {
  telInput.addEventListener('input', () => {
    telInput.value = telInput.value.replace(/\D/g, '').slice(0, 9);
  });
}

if (nombreInput) {
  nombreInput.addEventListener('input', () => {
    nombreInput.value = nombreInput.value.replace(/[0-9]/g, '');
  });
}

if (regForm) {
  regForm.addEventListener('submit', doRegistro);
}
