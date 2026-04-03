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
    const options = { method, headers: this.headers() };
    if (body) options.body = JSON.stringify(body);
    
    try {
      const res = await fetch(this.BASE + path, options);
      if (!res.ok) {
        let msg = `Error ${res.status}`;
        try { const errObj = await res.json(); msg = errObj.message || msg; } catch(e) {}
        throw new Error(msg);
      }
      // Algunos endpoints pueden no devolver JSON
      const text = await res.text();
      return text ? JSON.parse(text) : {};
    } catch (err) {
      Toast.err(`API Error: ${err.message}`);
      throw err;
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
  isAdmin: () => {
    const token = Auth.token();
    if (!token) return false;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return (payload.roles || []).includes('ROLE_ADMIN');
    } catch { return false; }
  },
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

  if (navLogin)  navLogin.style.display  = loggedIn ? 'none' : '';
  if (navLogout) navLogout.style.display = loggedIn ? '' : 'none';
  if (navAdmin)  navAdmin.style.display  = isAdmin  ? '' : 'none';
  if (navUser)   navUser.textContent     = Auth.email() || '';
  if (navPerfil) navPerfil.style.display = loggedIn ? '' : 'none';
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
document.addEventListener('DOMContentLoaded', updateNav);
