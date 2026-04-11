const chatState = {
  pedidoId: null,
  pollInterval: null,
  loading: false
};

function c(id) {
  return document.getElementById(id);
}

function sanitize(text) {
  return String(text || '').replace(/[&<>"']/g, (ch) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[ch]));
}

function getPedidoIdFromUrl() {
  const params = new URLSearchParams(window.location.search);
  const id = Number(params.get('pedidoId'));
  return Number.isFinite(id) && id > 0 ? id : null;
}

function renderEmpty(message) {
  c('chat-messages').innerHTML = `<p class="text-center text-secondary my-auto">${sanitize(message)}</p>`;
}

function renderMessages(msgs) {
  const root = c('chat-messages');
  if (!msgs.length) {
    renderEmpty('No hay mensajes aun. Envia el primero.');
    return;
  }

  const wasAtBottom = root.scrollHeight - root.scrollTop <= root.clientHeight + 50;

  root.innerHTML = msgs.map((m) => `
    <div class="chat-bubble ${m.esMio ? 'mine' : 'theirs'}">
      <div class="fw-semibold small">${sanitize(m.esMio ? 'Tu' : (m.nombreUsuario || 'Soporte'))}</div>
      <div>${sanitize(m.mensaje)}</div>
      <div class="chat-meta">${fmt.dt(m.fecha)}</div>
    </div>`).join('');

  if (wasAtBottom) root.scrollTop = root.scrollHeight;
}

async function loadMessages(showErrors = false) {
  if (chatState.loading) return;
  chatState.loading = true;
  try {
    const msgs = await API.get('/api/chat/' + chatState.pedidoId);
    renderMessages(Array.isArray(msgs) ? msgs : []);
  } catch (error) {
    if (showErrors) Toast.err(error.message || 'No se pudieron cargar mensajes.');
    renderEmpty('No se pudo cargar el chat en este momento.');
  } finally {
    chatState.loading = false;
  }
}

async function sendMessage() {
  const input = c('chat-input');
  const msg = input.value.trim();
  if (!msg) return;

  const btn = c('btn-chat-send');
  btn.disabled = true;
  input.value = '';

  try {
    await API.post('/api/chat/' + chatState.pedidoId, { mensaje: msg });
    await loadMessages();
  } catch (error) {
    Toast.err(error.message || 'No se pudo enviar el mensaje.');
    input.value = msg;
  } finally {
    btn.disabled = false;
  }
}

function bindEvents() {
  c('btn-chat-refresh').addEventListener('click', () => loadMessages(true));
  c('btn-chat-send').addEventListener('click', sendMessage);
  c('chat-input').addEventListener('keydown', (event) => {
    if (event.key === 'Enter') {
      event.preventDefault();
      sendMessage();
    }
  });
}

function startPolling() {
  if (chatState.pollInterval) clearInterval(chatState.pollInterval);
  chatState.pollInterval = setInterval(() => loadMessages(false), 5000);
}

function stopPolling() {
  if (chatState.pollInterval) {
    clearInterval(chatState.pollInterval);
    chatState.pollInterval = null;
  }
}

function initChat() {
  if (!Auth.requireClient()) return;

  chatState.pedidoId = getPedidoIdFromUrl();
  if (!chatState.pedidoId) {
    window.location.href = '/pages/cliente/perfil.html#tab-historial';
    return;
  }

  c('chat-pedido-code').textContent = '#' + chatState.pedidoId;
  bindEvents();
  loadMessages(true);
  startPolling();

  window.addEventListener('beforeunload', stopPolling);
}

document.addEventListener('DOMContentLoaded', initChat);
