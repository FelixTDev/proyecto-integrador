(function () {
  window.CC = window.CC || {};

  let container;

  function getContainer() {
    if (container) return container;
    container = document.createElement('div');
    container.id = 'toast-container';
    document.body.appendChild(container);
    return container;
  }

  function sanitize(msg) {
    return String(msg || '').replace(/[&<>"']/g, function (char) {
      return {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
      }[char];
    });
  }

  function show(message, type, duration) {
    const tone = type || 'info';
    const timeout = Number(duration || 3500);
    const el = document.createElement('div');
    const icon = tone === 'ok' ? 'bi-check-circle-fill' : tone === 'err' ? 'bi-x-circle-fill' : 'bi-info-circle-fill';
    el.className = 'toast toast-' + tone;
    el.innerHTML = '<i class="bi ' + icon + '"></i><span>' + sanitize(message) + '</span>';
    getContainer().appendChild(el);

    setTimeout(function () {
      el.classList.add('out');
      setTimeout(function () {
        el.remove();
      }, 300);
    }, timeout);
  }

  window.CC.toast = {
    show,
    ok(message, duration) {
      show(message, 'ok', duration);
    },
    err(message, duration) {
      show(message, 'err', duration);
    },
    info(message, duration) {
      show(message, 'info', duration);
    }
  };
})();
