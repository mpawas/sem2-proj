// Thin fetch wrapper for the scheduler REST API.

const BASE = '/api';

async function request(path, options = {}) {
  const res = await fetch(`${BASE}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  if (!res.ok) {
    let detail;
    try {
      detail = await res.json();
    } catch {
      detail = { message: `HTTP ${res.status}` };
    }
    throw new Error(detail.message || `HTTP ${res.status}`);
  }
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

export const api = {
  list: () => request('/tasks'),
  stats: () => request('/stats'),
  get: (id) => request(`/tasks/${id}`),
  submit: (payload) =>
    request('/tasks', { method: 'POST', body: JSON.stringify(payload) }),
  cancel: (id) => request(`/tasks/${id}`, { method: 'DELETE' }),
  clearFinished: () => request('/tasks', { method: 'DELETE' }),
  retry: (id) => request(`/tasks/${id}/retry`, { method: 'POST' }),
  burst: (count) => request(`/tasks/burst?count=${count}`, { method: 'POST' }),
  health: () => request('/health'),
};

/**
 * Opens the Server-Sent-Events stream. onTask is called with a TaskView JSON
 * on every transition; returns a handle with close() for cleanup.
 */
export function openEventStream(onTask) {
  const source = new EventSource(`${BASE}/events`);
  source.addEventListener('task', (event) => {
    try {
      onTask(JSON.parse(event.data));
    } catch {
      // transient decode error - ignore this frame
    }
  });
  return {
    close: () => source.close(),
    readyState: () => source.readyState,
  };
}