(function () {
  function getReasonMessage() {
    const params = new URLSearchParams(window.location.search);
    const reason = params.get('reason');
    if (reason === 'expired') return 'Tu sesion expiro. Ingresa nuevamente.';
    return '';
  }

  function setLoading(button, loading) {
    if (!button) return;
    button.disabled = loading;
    button.innerHTML = loading
      ? '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Ingresando...'
      : 'Ingresar';
  }

  async function doLogin(event) {
    event.preventDefault();

    const emailInput = document.getElementById('f-email');
    const passwordInput = document.getElementById('f-pwd');
    const button = document.getElementById('btn-submit');

    const email = (emailInput?.value || '').trim().toLowerCase();
    const password = (passwordInput?.value || '').trim();

    if (!email || !password) {
      Toast.err('Ingresa correo y contrasena.');
      return;
    }

    setLoading(button, true);

    try {
      const res = await API.post('/api/auth/login', { email, password }, { auth: false, retryOn401: false });
      const authPayload = parseLoginResponse(res);

      if (!authPayload?.token) {
        throw new Error('Token no recibido en login');
      }

      Auth.save(authPayload.token, authPayload.email || email);
      Toast.ok('Ingreso exitoso.');

      setTimeout(function () {
        window.location.href = Auth.isAdmin() ? '/pages/admin/dashboard.html' : '/index.html';
      }, 300);
    } catch (error) {
      Toast.err(error.message || 'No se pudo iniciar sesion.');
      setLoading(button, false);
    }
  }

  function initLogin() {
    if (Auth.isLoggedIn()) {
      window.location.href = Auth.isAdmin() ? '/pages/admin/dashboard.html' : '/index.html';
      return;
    }

    const reasonMessage = getReasonMessage();
    if (reasonMessage) {
      Toast.info(reasonMessage);
    }

    const form = document.getElementById('loginForm');
    if (!form) return;

    form.addEventListener('submit', doLogin);
  }

  document.addEventListener('DOMContentLoaded', initLogin);
})();
