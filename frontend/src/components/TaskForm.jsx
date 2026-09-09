import { useState } from 'react';
import { PRIORITY_META } from '../status.js';

const PRIORITIES = ['LOW', 'NORMAL', 'HIGH', 'CRITICAL'];
const TYPES = ['DELAY', 'COMPUTE', 'FETCH', 'BUILD_REPORT'];

// Params tailored per job type so the form stays approachable.
const PARAM_UI = {
  DELAY: { durationMs: { kind: 'number', label: 'Duration (ms)', min: 100, max: 120000, step: 100, value: 5000 } },
  COMPUTE: { limit: { kind: 'number', label: 'Sieve limit', min: 10000, max: 50000000, step: 10000, value: 2000000 } },
  FETCH: { url: { kind: 'text', label: 'URL to simulate', value: 'https://example.com/api/v1/data' } },
  BUILD_REPORT: {},
};

function initialForm() {
  return {
    name: '',
    type: 'DELAY',
    priority: 'NORMAL',
    delayMillis: 0,
    timeoutMillis: 60000,
    maxRetries: 0,
    failRate: 0,
    params: {},
  };
}

export default function TaskForm({ onSubmit, onBurst }) {
  const [form, setForm] = useState(initialForm);

  const params = form.params || {};

  function setParam(key, value) {
    setForm((prev) => {
      const next = { ...prev.params, [key]: value };
      // keep it clean when switching types
      return { ...prev, params: next };
    });
  }

  function submit(e) {
    e.preventDefault();
    const payload = {
      name: form.name || `${form.type}-task`,
      type: form.type,
      priority: form.priority,
      delayMillis: Number(form.delayMillis),
      timeoutMillis: Number(form.timeoutMillis),
      maxRetries: Number(form.maxRetries),
      failRate: Number(form.failRate),
      params: { ...form.params },
    };
    onSubmit(payload);
  }

  function renderParamInputs() {
    const ui = PARAM_UI[form.type] || {};
    return Object.entries(ui).map(([key, spec]) =>
      spec.kind === 'number'
        ? (
          <label className="field">
            <span>{spec.label}</span>
            <input
              type="number"
              min={spec.min}
              max={spec.max}
              step={spec.step}
              value={params[key] ?? spec.value}
              onChange={(e) => setParam(key, Number(e.target.value))}
            />
            <small>{spec.min} – {spec.max}</small>
          </label>
        )
        : (
          <label className="field field--wide">
            <span>{spec.label}</span>
            <input
              type="text"
              value={params[key] ?? spec.value}
              onChange={(e) => setParam(key, e.target.value)}
            />
          </label>
        ));
  }

  return (
    <form className="task-form" onSubmit={submit}>
      <h3>Submit a task</h3>

      <label className="field field--wide">
        <span>Name</span>
        <input
          type="text"
          value={form.name}
          placeholder="e.g. nightly-report"
          onChange={(e) => setForm({ ...form, name: e.target.value })}
        />
      </label>

      <label className="field">
        <span>Type</span>
        <select
          value={form.type}
          onChange={(e) => setForm({ ...form, type: e.target.value, params: {} })}
        >
          {TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
        </select>
      </label>

      <label className="field">
        <span>Priority</span>
        <select
          value={form.priority}
          onChange={(e) => setForm({ ...form, priority: e.target.value })}
        >
          {PRIORITIES.map((p) => (
            <option key={p} value={p}>{p} ({PRIORITY_META[p].rank})</option>
          ))}
        </select>
      </label>

      <label className="field">
        <span>Start delay (ms)</span>
        <input
          type="number" min={0} max={86400000} step={100}
          value={form.delayMillis}
          onChange={(e) => setForm({ ...form, delayMillis: e.target.value })}
        />
      </label>

      <label className="field">
        <span>Timeout (ms)</span>
        <input
          type="number" min={100} max={3600000} step={1000}
          value={form.timeoutMillis}
          onChange={(e) => setForm({ ...form, timeoutMillis: e.target.value })}
        />
      </label>

      <label className="field">
        <span>Max retries</span>
        <input
          type="number" min={0} max={10}
          value={form.maxRetries}
          onChange={(e) => setForm({ ...form, maxRetries: e.target.value })}
        />
      </label>

      {renderParamInputs()}

      <label className="field field--wide">
        <span>Failure rate: {form.failRate}%</span>
        <input
          type="range" min={0} max={100} step={5}
          value={form.failRate}
          onChange={(e) => setForm({ ...form, failRate: e.target.value })}
        />
        <small>Randomly fails this % of executions (demo retries)</small>
      </label>

      <div className="form-actions">
        <button className="btn btn--primary" type="submit">▶ Submit task</button>
        <button
          className="btn btn--ghost"
          type="button"
          onClick={() => onBurst(10)}
          title="Producer-consumer demo: 10 synthetic tasks"
        >
          🎲 Burst ×10
        </button>
      </div>
    </form>
  );
}