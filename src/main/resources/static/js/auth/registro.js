(function () {
  function validarRegistro(data) {
    if (!data.nombre) return 'Ingresa tus nombres completos.';
    if (/\d/.test(data.nombre)) return 'El nombre no debe contener numeros.';
    if (!/^[A-Za-z\u00C0-\u024F\s]+$/.test(data.nombre)) return 'El nombre solo debe contener letras y espacios.';
    if (!data.email) return 'Ingresa tu correo electronico.';
    if (!data.password || data.password.length < 6) return 'La contrasena debe tener al menos 6 caracteres.';
    if (data.telefono && !/^\d{9}$/.test(data.telefono)) return 'El celular debe tener 9 digitos.';
    return null;
  }

  function setLoading(button, loading) {
    if (!button) return;
    button.disabled = loading;
    button.innerHTML = loading
      ? '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Registrando...'
      : 'Crear mi cuenta';
  }

  function bindInputMasks() {
    const telInput = document.getElementById('f-tel');
    const nombreInput = document.getElementById('f-nombre');

    if (telInput) {
      telInput.addEventListener('input', function () {
        telInput.value = telInput.value.replace(/\D/g, '').slice(0, 9);
      });
    }

    if (nombreInput) {
      nombreInput.addEventListener('input', function () {
        nombreInput.value = nombreInput.value.replace(/\d/g, '');
      });
    }
  }

  async function submitRegistro(event) {
    event.preventDefault();

    const nombre = (document.getElementById('f-nombre')?.value || '').trim();
    const email = (document.getElementById('f-email')?.value || '').trim().toLowerCase();
    const password = (document.getElementById('f-pwd')?.value || '').trim();
    const telefono = (document.getElementById('f-tel')?.value || '').trim();
    const button = document.getElementById('btn-submit');

    const error = validarRegistro({ nombre, email, password, telefono });
    if (error) {
      Toast.err(error);
      return;
    }

    setLoading(button, true);

    try {
      const res = await API.post('/api/auth/registro', {
        nombre,
        email,
        password,
        telefono: telefono || null
      }, { auth: false, retryOn401: false });

      const authPayload = parseLoginResponse(res);
      if (!authPayload?.token) {
        throw new Error('Registro sin token');
      }

      Auth.save(authPayload.token, authPayload.email || email);
      Toast.ok('Cuenta creada correctamente.');
      setTimeout(function () {
        window.location.href = '/index.html';
      }, 350);
    } catch (apiError) {
      Toast.err(apiError.message || 'No se pudo completar el registro.');
      setLoading(button, false);
    }
  }

  function initRegistro() {
    if (Auth.isLoggedIn()) {
      window.location.href = '/index.html';
      return;
    }

    bindInputMasks();

    const form = document.getElementById('regForm');
    if (!form) return;
    form.addEventListener('submit', submitRegistro);
  }

  document.addEventListener('DOMContentLoaded', initRegistro);
})();
