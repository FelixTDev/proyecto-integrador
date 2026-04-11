(function () {
  window.CC = window.CC || {};

  const config = window.CC.config;
  const session = window.CC.session;
  const toast = window.CC.toast;

  let refreshPromise = null;

  function normalizePath(path) {
    return String(path || '').startsWith('/') ? String(path) : '/' + String(path || '');
  }

  function buildHeaders({ auth, tokenOverride } = {}) {
    const headers = {
      'Content-Type': 'application/json'
    };

    if (auth) {
      const token = tokenOverride || session.token();
      if (token) {
        headers.Authorization = 'Bearer ' + token;
      }
    }

    return headers;
  }

  function parseErrorMessage(payload, fallback) {
    if (!payload || typeof payload !== 'object') return fallback;

    const base = payload.message || fallback;
    if (!payload.data || typeof payload.data !== 'object') return base;

    const values = Object.values(payload.data);
    const detail = values.find(Boolean);
    return detail ? base + ': ' + detail : base;
  }

  async function parseResponse(res) {
    if (res.status === 204) return null;

    const text = await res.text();
    if (!text) return null;

    let payload;
    try {
      payload = JSON.parse(text);
    } catch {
      return text;
    }

    if (payload && typeof payload === 'object' && Object.prototype.hasOwnProperty.call(payload, 'data')) {
      return payload.data;
    }

    return payload;
  }

  async function refreshToken() {
    if (refreshPromise) return refreshPromise;

    const token = session.token();
    if (!token) {
      throw new Error('No hay token para refresh');
    }

    refreshPromise = fetch(config.apiBaseUrl + '/api/auth/refresh', {
      method: 'POST',
      headers: buildHeaders({ auth: true, tokenOverride: token })
    })
      .then(async function (res) {
        if (!res.ok) {
          throw new Error('No se pudo refrescar la sesion');
        }

        const payload = await parseResponse(res);
        const newToken = payload?.token;
        if (!newToken) {
          throw new Error('Refresh sin token');
        }

        session.save({ token: newToken, email: payload?.email || session.email() });
        return newToken;
      })
      .finally(function () {
        refreshPromise = null;
      });

    return refreshPromise;
  }

  function isProtectedPath(path) {
    return /^\/api\/(admin|cliente|carrito|pedido|pedidos|pago|pagos|chat)\b/.test(path);
  }

  function goToLogin(reason) {
    const current = window.location.pathname || '';
    if (current.endsWith(config.loginPath)) return;
    const suffix = reason ? '?' + config.authReasonQueryKey + '=' + encodeURIComponent(reason) : '';
    window.location.href = config.loginPath + suffix;
  }

  async function request(method, path, body, options) {
    const normalizedPath = normalizePath(path);
    const cfg = options || {};
    const authEnabled = cfg.auth !== false;
    const canRetry = cfg.retryOn401 !== false;

    const res = await fetch(config.apiBaseUrl + normalizedPath, {
      method,
      headers: buildHeaders({ auth: authEnabled }),
      body: body === undefined || body === null ? undefined : JSON.stringify(body)
    });

    if (res.ok) {
      return parseResponse(res);
    }

    const rawError = await parseResponse(res);
    const errorMessage = typeof rawError === 'string'
      ? rawError
      : parseErrorMessage(rawError, 'Error ' + res.status);

    const isAuthPath = normalizedPath.startsWith('/api/auth/');

    if (res.status === 401 && authEnabled && !isAuthPath && canRetry) {
      try {
        await refreshToken();
        return request(method, normalizedPath, body, {
          auth: authEnabled,
          retryOn401: false
        });
      } catch {
        session.clear();
        if (isProtectedPath(normalizedPath)) {
          goToLogin('expired');
        }
        throw new Error('Tu sesion expiro. Inicia sesion nuevamente.');
      }
    }

    if (res.status === 401) {
      session.clear();
      if (isProtectedPath(normalizedPath)) {
        goToLogin('expired');
      }
    }

    if (res.status === 403 && isProtectedPath(normalizedPath)) {
      window.location.href = config.homePath;
    }

    throw new Error(errorMessage);
  }

  const http = {
    request,
    get(path, options) {
      return request('GET', path, null, options);
    },
    post(path, body, options) {
      return request('POST', path, body, options);
    },
    put(path, body, options) {
      return request('PUT', path, body, options);
    },
    patch(path, body, options) {
      return request('PATCH', path, body, options);
    },
    delete(path, options) {
      return request('DELETE', path, null, options);
    },
    async logout() {
      try {
        await this.post('/api/auth/logout', {}, { auth: true, retryOn401: false });
      } catch (error) {
        if (error?.message) {
          toast.info(error.message);
        }
      }
    }
  };

  window.CC.http = http;
})();
