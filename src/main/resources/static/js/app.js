/* ═══════════════════════════════════════════════
   CASA CHANTILLY — Shared Utilities
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

  async get(path) {
    const r = await fetch(this.BASE + path, { headers: this.headers() });
    return r.json();
  },

  async post(path, body) {
    const r = await fetch(this.BASE + path, {
      method: 'POST', headers: this.headers(), body: JSON.stringify(body)
    });
    return r.json();
  },

  async put(path, body) {
    const r = await fetch(this.BASE + path, {
      method: 'PUT', headers: this.headers(), body: JSON.stringify(body)
    });
    return r.json();
  },

  async patch(path, body) {
    const r = await fetch(this.BASE + path, {
      method: 'PATCH', headers: this.headers(), body: body ? JSON.stringify(body) : undefined
    });
    return r.json();
  },

  async delete(path) {
    const r = await fetch(this.BASE + path, { method: 'DELETE', headers: this.headers() });
    return r.json();
  }
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
    localStorage.setItem('email', email);
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

/* ── Toast ── */
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
    const icons = { ok: 'bi-check-circle-fill', err: 'bi-exclamation-circle-fill', info: 'bi-info-circle-fill' };
    const t = document.createElement('div');
    t.className = `toast toast-${type}`;
    t.innerHTML = `<i class="bi ${icons[type] || icons.info}" style="color:var(--${type==='ok'?'green':type==='err'?'red':'blue'})"></i>${msg}`;
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

/* ── Modal helpers ── */
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

// Close modal on backdrop click
document.addEventListener('click', e => {
  if (e.target.classList.contains('modal-backdrop')) Modal.closeAll();
});

/* ── Nav update ── */
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

/* ── Format helpers ── */
const fmt = {
  money: (v) => 'S/ ' + Number(v || 0).toFixed(2),
  date:  (s) => s ? new Date(s).toLocaleDateString('es-PE', { day:'2-digit', month:'short', year:'numeric' }) : '—',
  dt:    (s) => s ? new Date(s).toLocaleString('es-PE', { day:'2-digit', month:'short', year:'numeric', hour:'2-digit', minute:'2-digit' }) : '—',
};

/* ── Run on DOM ready ── */
document.addEventListener('DOMContentLoaded', updateNav);
