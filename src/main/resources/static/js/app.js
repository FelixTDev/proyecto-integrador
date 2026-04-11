(function () {
  window.CC = window.CC || {};

  const config = window.CC.config;
  const session = window.CC.session;
  const http = window.CC.http;
  const toast = window.CC.toast;
  const format = window.CC.format;

  function ensureCore() {
    const missing = [];
    if (!config) missing.push('config');
    if (!session) missing.push('session');
    if (!http) missing.push('http');
    if (!toast) missing.push('toast');
    if (!format) missing.push('format');
    if (missing.length) throw new Error('Faltan modulos core: ' + missing.join(', '));
  }

  function isVendorOnly() {
    return session.hasRole('ROLE_VENDEDOR') && !session.hasRole('ROLE_ADMIN');
  }

  function hideAdminOnlyLinksForVendor() {
    if (!isVendorOnly()) return;
    const restricted = [
      'productos.html',
      'inventario.html',
      'promociones.html',
      'usuarios.html'
    ];
    document.querySelectorAll('.sidebar a').forEach(function (link) {
      const href = String(link.getAttribute('href') || '');
      if (restricted.some(function (r) { return href.endsWith(r); })) {
        link.style.display = 'none';
      }
    });
  }

  function enforceAdminSubmodulePermissions() {
    if (!isVendorOnly()) return;
    const path = window.location.pathname || '';
    const blocked = [
      '/pages/admin/productos.html',
      '/pages/admin/inventario.html',
      '/pages/admin/promociones.html',
      '/pages/admin/usuarios.html'
    ];
    if (blocked.some(function (p) { return path.endsWith(p); })) {
      window.location.href = '/pages/admin/dashboard.html';
    }
  }

  function updateNav() {
    const loggedIn = session.isLoggedIn();
    const isAdmin = session.isAdmin();

    const navLogin = document.getElementById('nav-login');
    const navLogout = document.getElementById('nav-logout');
    const navAdmin = document.getElementById('nav-admin');
    const navUser = document.getElementById('nav-user');
    const navPerfil = document.getElementById('nav-perfil');
    const navReclamos = document.getElementById('nav-reclamos');

    if (navLogin) navLogin.style.display = loggedIn ? 'none' : '';
    if (navLogout) navLogout.style.display = loggedIn ? '' : 'none';
    if (navAdmin) navAdmin.style.display = isAdmin ? '' : 'none';
    if (navUser) navUser.textContent = session.email() || '';
    if (navPerfil) navPerfil.style.display = loggedIn ? '' : 'none';
    if (navReclamos) navReclamos.style.display = loggedIn && session.isClient() ? '' : 'none';
  }

  function enforcePageAccessByRole() {
    const path = window.location.pathname || '';

    if (path.includes('/pages/admin/') && !session.isAdmin()) {
      window.location.href = config.homePath;
      return;
    }

    if (path.includes('/pages/cliente/')) {
      if (!session.isLoggedIn()) {
        window.location.href = config.loginPath;
        return;
      }
      if (!session.isClient()) {
        window.location.href = session.isAdmin() ? config.adminPath : config.homePath;
      }
    }
  }

  async function doLogout() {
    if (session.isLoggedIn()) await http.logout();
    session.clear();
    window.location.href = config.homePath;
  }

  const modal = {
    open(id) {
      const el = document.getElementById(id);
      if (el) el.classList.add('open');
    },
    close(id) {
      const el = document.getElementById(id);
      if (el) el.classList.remove('open');
    },
    closeAll() {
      document.querySelectorAll('.modal-backdrop.open').forEach(function (m) {
        m.classList.remove('open');
      });
    }
  };

  const authApi = {
    token() { return session.token(); },
    email() { return session.email(); },
    roles() { return session.roles(); },
    hasRole(role) { return session.hasRole(role); },
    isAdmin() { return session.isAdmin(); },
    isClient() { return session.isClient(); },
    isLoggedIn() { return session.isLoggedIn(); },
    save(token, email) { session.save({ token: token, email: email }); },
    clear() { session.clear(); },
    requireLogin() {
      if (!session.isLoggedIn()) {
        window.location.href = config.loginPath;
        return false;
      }
      return true;
    },
    requireAdmin() {
      if (!session.isAdmin()) {
        window.location.href = config.homePath;
        return false;
      }
      return true;
    },
    requireClient() {
      if (!session.isLoggedIn()) {
        window.location.href = config.loginPath;
        return false;
      }
      if (!session.isClient()) {
        window.location.href = session.isAdmin() ? config.adminPath : config.homePath;
        return false;
      }
      return true;
    },
    requireRole(roleName) {
      if (!session.hasRole(roleName)) {
        window.location.href = config.adminPath;
        return false;
      }
      return true;
    }
  };

  function parseLoginResponse(response) {
    if (!response || typeof response !== 'object') return null;
    return { token: response.token || null, email: response.email || null };
  }

  function boot() {
    ensureCore();

    window.API = {
      BASE: config.apiBaseUrl,
      request: http.request.bind(http),
      get: http.get.bind(http),
      post: http.post.bind(http),
      put: http.put.bind(http),
      patch: http.patch.bind(http),
      delete: http.delete.bind(http)
    };

    window.Auth = authApi;
    window.Toast = toast;
    window.Modal = modal;
    window.fmt = {
      money: format.money,
      date: format.date,
      dt: format.datetime
    };
    window.parseLoginResponse = parseLoginResponse;
    window.doLogout = doLogout;
    window.updateNav = updateNav;

    document.addEventListener('click', function (event) {
      if (event.target.classList.contains('modal-backdrop')) modal.closeAll();
      const closeBtn = event.target.closest('[data-close-modal]');
      if (closeBtn) modal.close(closeBtn.getAttribute('data-close-modal'));
    });

    document.addEventListener('DOMContentLoaded', function () {
      enforcePageAccessByRole();
      enforceAdminSubmodulePermissions();
      hideAdminOnlyLinksForVendor();
      updateNav();
      const navLogoutBtn = document.getElementById('nav-logout');
      if (navLogoutBtn && !navLogoutBtn.getAttribute('onclick') && !navLogoutBtn.dataset.boundLogout) {
        navLogoutBtn.dataset.boundLogout = '1';
        navLogoutBtn.addEventListener('click', doLogout);
      }

    });
  }

  boot();
})();



