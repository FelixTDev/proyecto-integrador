(function () {
  window.CC = window.CC || {};

  const config = window.CC.config;
  const storage = window.CC.storage;

  function parseTokenPayload(token) {
    if (!token) return null;
    try {
      const payloadPart = token.split('.')[1];
      if (!payloadPart) return null;
      const base64 = payloadPart.replace(/-/g, '+').replace(/_/g, '/');
      const decoded = atob(base64);
      return JSON.parse(decoded);
    } catch {
      return null;
    }
  }

  const session = {
    token() {
      return storage.get(config.tokenStorageKey);
    },
    email() {
      return storage.get(config.emailStorageKey) || '';
    },
    payload() {
      return parseTokenPayload(this.token());
    },
    roles() {
      const payload = this.payload();
      return Array.isArray(payload?.roles) ? payload.roles : [];
    },
    hasRole(role) {
      return this.roles().includes(role);
    },
    isAdmin() {
      return this.hasRole('ROLE_ADMIN') || this.hasRole('ROLE_VENDEDOR');
    },
    isClient() {
      return this.hasRole('ROLE_CLIENTE');
    },
    isLoggedIn() {
      return Boolean(this.token());
    },
    save({ token, email }) {
      if (token) storage.set(config.tokenStorageKey, token);
      storage.set(config.emailStorageKey, email || '');
    },
    clear() {
      storage.remove(config.tokenStorageKey);
      storage.remove(config.emailStorageKey);
    }
  };

  window.CC.session = session;
})();
