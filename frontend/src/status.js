// Live status helpers shared across components.

export const STATUS_ORDER = [
  'PENDING',
  'QUEUED',
  'RUNNING',
  'COMPLETED',
  'FAILED',
  'CANCELLED',
  'TIMED_OUT',
];

export const STATUS_META = {
  PENDING:   { label: 'Pending',   color: '#8e8ea8', pulse: false },
  QUEUED:    { label: 'Queued',    color: '#5b7fd6', pulse: false },
  RUNNING:   { label: 'Running',   color: '#2fae5e', pulse: true },
  COMPLETED: { label: 'Done',      color: '#1f8f4d', pulse: false },
  FAILED:    { label: 'Failed',    color: '#d64545', pulse: false },
  CANCELLED: { label: 'Cancelled', color: '#9b6b3f', pulse: false },
  TIMED_OUT: { label: 'Timed out', color: '#c77b2c', pulse: false },
};

export const PRIORITY_META = {
  LOW:      { rank: 0, label: 'LOW',      color: '#64748b' },
  NORMAL:   { rank: 1, label: 'NORMAL',   color: '#3b82f6' },
  HIGH:     { rank: 2, label: 'HIGH',     color: '#f59e0b' },
  CRITICAL: { rank: 3, label: 'CRITICAL', color: '#ef4444' },
};

export const TYPE_META = {
  DELAY:        { label: 'Delay',        color: '#22c55e' },
  COMPUTE:      { label: 'Compute',      color: '#a855f7' },
  FETCH:        { label: 'Fetch',        color: '#06b6d4' },
  BUILD_REPORT: { label: 'Report',       color: '#f97316' },
};

export function formatMillis(ms) {
  if (!ms || ms <= 0) return '—';
  if (ms < 1000) return `${ms} ms`;
  if (ms < 60_000) return `${(ms / 1000).toFixed(1)} s`;
  return `${(ms / 60_000).toFixed(1)} min`;
}

export function formatClock(epochMillis) {
  if (!epochMillis || epochMillis <= 0) return '—';
  const d = new Date(epochMillis);
  const pad = (n) => String(n).padStart(2, '0');
  return `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
}