import StatusBadge from './StatusBadge.jsx';
import {
  TYPE_META,
  PRIORITY_META,
  formatMillis,
  formatClock,
} from '../status.js';

export default function TaskTable({ tasks, onSelect, onCancel, onRetry }) {
  if (!tasks.length) {
    return <div className="table-empty">No tasks yet — submit one on the left.</div>;
  }
  return (
    <table className="task-table">
      <thead>
        <tr>
          <th>Priority</th>
          <th>Name</th>
          <th>Type</th>
          <th>Status</th>
          <th>Attempt</th>
          <th>Elapsed</th>
          <th>Finished</th>
          <th>Result / Error</th>
          <th />
        </tr>
      </thead>
      <tbody>
        {tasks.map((t) => (
          <tr
            key={t.id}
            onClick={() => onSelect(t.id)}
            className={t.status === 'RUNNING' ? 'row--running' : ''}
          >
            <td>
              <span
                className="prio-pill"
                style={{
                  background: PRIORITY_META[t.priority].color + '1f',
                  color: PRIORITY_META[t.priority].color,
                }}
              >
                {t.priority}
              </span>
            </td>
            <td className="cell--name">
              <span className="mono">{short(t.id)}</span>
              <strong>{t.name}</strong>
            </td>
            <td>
              <span className="type-pill" style={{ color: TYPE_META[t.type].color }}>
                {TYPE_META[t.type].label}
              </span>
            </td>
            <td><StatusBadge status={t.status} progress={t.progress} /></td>
            <td className="mono">{t.attempt}</td>
            <td className="mono">{formatMillis(t.elapsedMillis)}</td>
            <td className="mono">{formatClock(t.finishedAtMillis)}</td>
            <td className="cell--result" title={t.result || t.error}>
              {t.result || t.error || '…'}
            </td>
            <td className="cell--actions">
              {canCancel(t.status) && (
                <button
                  className="mini-btn mini-btn--danger"
                  title="Cancel task"
                  onClick={(e) => { e.stopPropagation(); onCancel(t.id); }}
                >
                  ✕
                </button>
              )}
              {canRetry(t.status) && (
                <button
                  className="mini-btn mini-btn--retry"
                  title="Retry task"
                  onClick={(e) => { e.stopPropagation(); onRetry(t.id); }}
                >
                  ⟳
                </button>
              )}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

function canCancel(status) {
  return ['PENDING', 'QUEUED', 'RUNNING'].includes(status);
}

function canRetry(status) {
  return ['FAILED', 'TIMED_OUT'].includes(status);
}

function short(id) {
  return `${id.slice(0, 8)}…`;
}