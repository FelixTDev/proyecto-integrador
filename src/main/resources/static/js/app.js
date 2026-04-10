/* ═══════════════════════════════════════════════
   LA CASA DEL CHANTILLY — Core App Logic (JS)
   ═══════════════════════════════════════════════ */

const API = {
  BASE: '',

  headers(auth = true) {
    const h = { 'Content-Type': 'application/json' };
    if (auth) {
      const jwt = Auth.token();
      if (jwt) h['Authorization'] = 'Bearer ' + jwt;
    }
    return h;
  },

  async request(method, path, body = null) {
    const normalizedPath = String(path || '').startsWith('/') ? String(path) : `/${String(path || '')}`;
    const options = { method, headers: this.headers() };
    if (body) options.body = JSON.stringify(body);
    
    try {
      const res = await fetch(this.BASE + normalizedPath, options);
      if (!res.ok) {
        let msg = `Error ${res.status}`;
        try {
          const errObj = await res.json();
          msg = errObj.message || msg;
          if (errObj.data && typeof errObj.data === 'object') {
            const firstError = Object.values(errObj.data)[0];
            if (firstError) msg = `${msg}: ${firstError}`;
          }
        } catch(e) {}
        this.handleAuthError(res.status, normalizedPath, msg);
        throw new Error(msg);
      }
      // Algunos endpoints pueden no devolver JSON
      const text = await res.text();
      if (!text) return {};
      const parsed = JSON.parse(text);
      // Normaliza respuestas backend envueltas en ApiResponse<T>
      if (parsed && typeof parsed === 'object' && Object.prototype.hasOwnProperty.call(parsed, 'data')) {
        return parsed.data;
      }
      return parsed;
    } catch (err) {
      Toast.err(`API Error: ${err.message}`);
      throw err;
    }
  },

  handleAuthError(status, path, message) {
    const isAuthPath = String(path || '').startsWith('/api/auth/');
    const isProtectedPath = /^\/api\/(admin|cliente|carrito|pedido|pedidos|pago|pagos)\b/.test(String(path || ''));
    if (isAuthPath) return;

    if (status === 401) {
      Auth.clear();
      if (isProtectedPath) {
        const current = window.location.pathname || '';
        if (!current.includes('/pages/auth/login.html')) {
          setTimeout(() => {
            window.location.href = '/pages/auth/login.html?reason=expired';
          }, 250);
        }
      }
    } else if (status === 403 && isProtectedPath) {
      const current = window.location.pathname || '';
      if (current.includes('/pages/admin/') && !Auth.isAdmin()) {
        setTimeout(() => {
          window.location.href = '/';
        }, 250);
      }
    }
  },

  async get(path) { return this.request('GET', path); },
  async post(path, body) { return this.request('POST', path, body); },
  async put(path, body) { return this.request('PUT', path, body); },
  async patch(path, body) { return this.request('PATCH', path, body); },
  async delete(path) { return this.request('DELETE', path); }
};

/* ── Auth ── */
const Auth = {
  token: ()  => localStorage.getItem('jwt'),
  email: ()  => localStorage.getItem('email'),
  roles: () => {
    const token = Auth.token();
    if (!token) return [];
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.roles || [];
    } catch { return []; }
  },
  hasRole: (role) => Auth.roles().includes(role),
  isAdmin: () => {
    return Auth.hasRole('ROLE_ADMIN') || Auth.hasRole('ROLE_VENDEDOR');
  },
  isClient: () => Auth.hasRole('ROLE_CLIENTE'),
  isLoggedIn: () => !!Auth.token(),
  save: (token, email) => {
    localStorage.setItem('jwt', token);
    localStorage.setItem('email', email || '');
  },
  clear: () => {
    localStorage.removeItem('jwt');
    localStorage.removeItem('email');
  },
  requireLogin: () => {
    if (!Auth.isLoggedIn()) { window.location.href = '/pages/auth/login.html'; return false; }
    return true;
  },
  requireAdmin: () => {
    if (!Auth.isAdmin()) { window.location.href = '/'; return false; }
    return true;
  },
  requireClient: () => {
    if (!Auth.isLoggedIn()) { window.location.href = '/pages/auth/login.html'; return false; }
    if (!Auth.isClient()) {
      window.location.href = Auth.isAdmin() ? '/pages/admin/dashboard.html' : '/';
      return false;
    }
    return true;
  }
};

/* ── Toast Notifications ── */
const Toast = {
  _container: null,
  _get() {
    if (!this._container) {
      this._container = document.createElement('div');
      this._container.id = 'toast-container';
      document.body.appendChild(this._container);
    }
    return this._container;
  },
  show(msg, type = 'info', duration = 3500) {
    const icons = { ok: 'bi-check-circle-fill', err: 'bi-x-circle-fill', info: 'bi-info-circle-fill' };
    const cssColor = type === 'ok' ? 'var(--green)' : type === 'err' ? 'var(--red)' : 'var(--gold)';
    const t = document.createElement('div');
    t.className = `toast toast-${type}`;
    t.innerHTML = `<i class="bi ${icons[type] || icons.info}" style="color:${cssColor};font-size:1.1rem"></i><span>${msg}</span>`;
    this._get().appendChild(t);
    setTimeout(() => {
      t.classList.add('out');
      setTimeout(() => t.remove(), 300);
    }, duration);
  },
  ok:   (msg) => Toast.show(msg, 'ok'),
  err:  (msg) => Toast.show(msg, 'err'),
  info: (msg) => Toast.show(msg, 'info'),
};

/* ── Modal Helpers ── */
const Modal = {
  open(id) {
    const el = document.getElementById(id);
    if (el) el.classList.add('open');
  },
  close(id) {
    const el = document.getElementById(id);
    if (el) el.classList.remove('open');
  },
  closeAll() {
    document.querySelectorAll('.modal-backdrop.open').forEach(m => m.classList.remove('open'));
  }
};

// Cerrar modal al hacer clic en el backdrop
document.addEventListener('click', e => {
  if (e.target.classList.contains('modal-backdrop')) Modal.closeAll();
});

/* ── UI / Navegación Dinámica ── */
function updateNav() {
  const loggedIn = Auth.isLoggedIn();
  const isAdmin  = Auth.isAdmin();

  const navLogin   = document.getElementById('nav-login');
  const navLogout  = document.getElementById('nav-logout');
  const navAdmin   = document.getElementById('nav-admin');
  const navUser    = document.getElementById('nav-user');
  const navPerfil  = document.getElementById('nav-perfil');
  const navReclamos = document.getElementById('nav-reclamos');

  if (navLogin)  navLogin.style.display  = loggedIn ? 'none' : '';
  if (navLogout) navLogout.style.display = loggedIn ? '' : 'none';
  if (navAdmin)  navAdmin.style.display  = isAdmin  ? '' : 'none';
  if (navUser)   navUser.textContent     = Auth.email() || '';
  if (navPerfil) navPerfil.style.display = loggedIn ? '' : 'none';
  if (navReclamos) navReclamos.style.display = (loggedIn && Auth.isClient()) ? '' : 'none';
}

function enforcePageAccessByRole() {
  const path = window.location.pathname || '';
  if (path.includes('/pages/admin/') && !Auth.isAdmin()) {
    window.location.href = '/';
    return;
  }
  if (path.includes('/pages/cliente/')) {
    if (!Auth.isLoggedIn()) {
      window.location.href = '/pages/auth/login.html';
      return;
    }
    if (!Auth.isClient()) {
      window.location.href = Auth.isAdmin() ? '/pages/admin/dashboard.html' : '/';
    }
  }
}

async function doLogout() {
  const jwt = Auth.token();
  if (jwt) {
    try { await API.post('/api/auth/logout', {}); } catch {}
  }
  Auth.clear();
  window.location.href = '/';
}

/* ── Formateadores ── */
const fmt = {
  money: (v) => 'S/ ' + Number(v || 0).toFixed(2),
  date:  (s) => s ? new Date(s).toLocaleDateString('es-PE', { day:'2-digit', month:'short', year:'numeric' }) : '—',
  dt:    (s) => s ? new Date(s).toLocaleString('es-PE', { day:'2-digit', month:'short', year:'numeric', hour:'2-digit', minute:'2-digit' }) : '—',
};

/* ── Run on DOM ready ── */
document.addEventListener('DOMContentLoaded', () => {
  enforcePageAccessByRole();
  updateNav();
});
