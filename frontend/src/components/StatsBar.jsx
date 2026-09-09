import { formatMillis, STATUS_META } from '../status.js';

const CARD_ORDER = [
  ['total', 'Total tasks', '#e2e8f0'],
  ['pending', 'Pending', '#8e8ea8'],
  ['queued', 'Queued', '#5b7fd6'],
  ['running', 'Running', '#2fae5e'],
  ['completed', 'Completed', '#1f8f4d'],
  ['failed', 'Failed', '#d64545'],
  ['cancelled', 'Cancelled', '#9b6b3f'],
  ['timedOut', 'Timed out', '#c77b2c'],
];

function card(key, label, color, value) {
  return (
    <div className="stat-card" key={key}>
      <span className="stat-value" style={{ color }}>{value}</span>
      <span className="stat-label">{label}</span>
    </div>
  );
}

export default function StatsBar({ stats }) {
  if (!stats) return null;
  const cards = CARD_ORDER.map(([key, label, color]) =>
    card(key, label, color, stats[key] ?? 0));
  return (
    <div className="stats-bar">
      <div className="stats-cards">
        {cards}
        {card('avg', 'Avg latency', '#3b82f6', formatMillis(stats.avgLatencyMillis))}
        {card('p95', 'P95 latency', '#f59e0b', formatMillis(stats.p95LatencyMillis))}
        {card('depth', 'Queue depth', '#06b6d4', `${stats.queueDepth} / ${stats.workerCount} workers`)}
      </div>
      <div className="live-tick">
        <span className="dot dot--live" /> LIVE
      </div>
    </div>
  );
}