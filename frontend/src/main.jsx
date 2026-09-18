import { useState, useEffect, useRef } from 'react';
import { createRoot } from 'react-dom/client';
import { api, openEventStream } from './api.js';
import TaskForm from './components/TaskForm.jsx';
import TaskTable from './components/TaskTable.jsx';
import StatsBar from './components/StatsBar.jsx';
import TaskDetailModal from './components/TaskDetailModal.jsx';
import './styles.css';

const FILTERS = ['ALL', 'PENDING', 'QUEUED', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED', 'TIMED_OUT'];

export default function App() {
  const [tasks, setTasks] = useState([]);
  const [stats, setStats] = useState(null);
  const [filter, setFilter] = useState('ALL');
  const [selectedId, setSelectedId] = useState(null);
  const [toast, setToast] = useState(null);
  const [connected, setConnected] = useState(false);

  const mapRef = useRef(new Map());
  const statsBusy = useRef(false);
  const statsDirty = useRef(false);
  const toastTimer = useRef(null);

  function commit(updatedMap) {
    setTasks([...updatedMap.values()].sort((a, b) => b.submittedAtMillis - a.submittedAtMillis));
  }

  function refreshStatsSoon() {
    if (statsBusy.current) {
      statsDirty.current = true;
      return;
    }
    statsBusy.current = true;
    api.stats()
      .then((s) => setStats(s))
      .catch(() => {})
      .finally(() => {
        statsBusy.current = false;
        if (statsDirty.current) {
          statsDirty.current = false;
          refreshStatsSoon();
        }
      });
  }

  function refreshTasks() {
    api.list().then((list) => {
      const m = new Map();
      list.forEach((t) => m.set(t.id, t));
      mapRef.current = m;
      commit(m);
      refreshStatsSoon();
    }).catch((e) => showToast(`Backend unreachable: ${e.message}`, 'err'));
  }

  function upsert(task) {
    mapRef.current.set(task.id, task);
    commit(mapRef.current);
    refreshStatsSoon();
  }

  function showToast(message, kind = 'info') {
    setToast({ message, kind });
    if (toastTimer.current) clearTimeout(toastTimer.current);
    toastTimer.current = setTimeout(() => setToast(null), 4000);
  }

  useEffect(() => {
    refreshTasks();

    const stream = openEventStream(upsert);
    setConnected(true);

    const ticker = setInterval(refreshStatsSoon, 5000);
    return () => {
      stream.close();
      clearInterval(ticker);
      if (toastTimer.current) clearTimeout(toastTimer.current);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const selected = tasks.find((t) => t.id === selectedId);
  const visible = filter === 'ALL' ? tasks : tasks.filter((t) => t.status === filter);

  return (
    <div className="app">
      <header className="app-header">
        <div className="brand">
          <span className="logo">⚡</span>
          <div>
            <h1>Async Task Scheduler</h1>
            <p>Java concurrency core · React dashboard · live via Server-Sent Events</p>
          </div>
        </div>
        <div className={`conn ${connected ? 'conn--ok' : 'conn--down'}`}>
          <span className="dot" />
          {connected ? 'live' : 'connecting…'}
        </div>
      </header>

      <StatsBar stats={stats} />

      <div className="layout">
        <aside className="panel">
          <TaskForm onSubmit={handleSubmit} onBurst={handleBurst} onClearFinished={handleClearFinished} />
          <div className="panel-notes">
            <h4>What is happening under the hood?</h4>
            <ul>
              <li>Tasks wait in a <strong>PriorityBlockingQueue</strong></li>
              <li><strong>CRITICAL</strong> tasks jump the queue (priority + FIFO)</li>
              <li>Your <strong>delay</strong> is fired by a <em>ScheduledExecutorService</em></li>
              <li>A <strong>timeout guard</strong> marks slow jobs <em>TIMED_OUT</em></li>
              <li><strong>failRate &gt; 0</strong> exercises automatic retries</li>
            </ul>
          </div>
        </aside>

        <main className="panel panel--wide">
          <div className="toolbar">
            <h2>Tasks</h2>
            <div className="filter-row">
              {FILTERS.map((f) => (
                <button
                  key={f}
                  className={`filter-chip ${f === filter ? 'filter-chip--active' : ''}`}
                  onClick={() => setFilter(f)}
                >
                  {f}
                </button>
              ))}
            </div>
          </div>
          <TaskTable
            tasks={visible}
            onSelect={setSelectedId}
            onCancel={handleCancel}
            onRetry={handleRetry}
          />
        </main>
      </div>

      <TaskDetailModal
        task={selected}
        onClose={() => setSelectedId(null)}
        onCancel={handleCancel}
        onRetry={handleRetry}
      />

      {toast && <div className={`toast toast--${toast.kind}`}>{toast.message}</div>}
    </div>
  );

  async function handleSubmit(payload) {
    try {
      const created = await api.submit(payload);
      showToast(`Submitted "${created.name}" · ${created.id.slice(0, 8)}…`, 'ok');
    } catch (e) {
      showToast(`Submit failed: ${e.message}`, 'err');
    }
  }

  async function handleCancel(id) {
    try {
      await api.cancel(id);
      showToast('Cancel request sent', 'ok');
    } catch (e) {
      showToast(`Cancel failed: ${e.message}`, 'err');
    }
  }

  async function handleRetry(id) {
    try {
      await api.retry(id);
      showToast('Task requeued for retry', 'ok');
    } catch (e) {
      showToast(`Retry failed: ${e.message}`, 'err');
    }
  }

  async function handleBurst(count) {
    try {
      const res = await api.burst(count);
      showToast(res.message, 'ok');
    } catch (e) {
      showToast(`Burst failed: ${e.message}`, 'err');
    }
  }

  async function handleClearFinished() {
    try {
      const res = await api.clearFinished();
      showToast(res.message, 'ok');
      // Removals do not produce SSE events, so re-sync the table + stats.
      refreshTasks();
    } catch (e) {
      showToast(`Clear failed: ${e.message}`, 'err');
    }
  }
}

// ---------------------------------------------------------------
// Entry point: mount the dashboard into #root (React 18 createRoot)
// ---------------------------------------------------------------
const rootElement = document.getElementById('root');
if (rootElement) {
  createRoot(rootElement).render(<App />);
}