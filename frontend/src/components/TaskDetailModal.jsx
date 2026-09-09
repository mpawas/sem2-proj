import StatusBadge from './StatusBadge.jsx';
import { PRIORITY_META, TYPE_META, formatClock, formatMillis } from '../status.js';

function row(label, value) {
  return (
    <div className="kv">
      <span className="kv-key">{label}</span>
      <span className="kv-value">{value}</span>
    </div>
  );
}

export default function TaskDetailModal({ task, onClose, onCancel, onRetry }) {
  if (!task) return null;
  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3>{task.name}</h3>
          <button className="mini-btn" onClick={onClose}>✕</button>
        </div>

        <div className="modal-body">
          <div className="kv-grid">
            {row('ID', <span className="mono">{task.id}</span>)}
            {row('Status', <StatusBadge status={task.status} progress={task.progress} />)}
            {row('Type', TYPE_META[task.type].label)}
            {row('Priority', `${task.priority} (rank ${PRIORITY_META[task.priority].rank})`)}
            {row('Attempt', task.attempt)}
            {row('Worker', <span className="mono">{task.workerId || '—'}</span>)}
            {row('Submitted', formatClock(task.submittedAtMillis))}
            {row('Started', formatClock(task.startedAtMillis))}
            {row('Finished', formatClock(task.finishedAtMillis))}
            {row('Elapsed', formatMillis(task.elapsedMillis))}
            {row('Delay', `${task.delayMillis} ms`)}
            {row('Timeout', `${task.timeoutMillis} ms`)}
            {row('Max retries', task.maxRetries)}
            {row('Fail rate', `${task.failRate}%`)}
          </div>

          <div className="modal-section">
            <h4>Parameters</h4>
            <pre>{JSON.stringify(task.params, null, 2)}</pre>
          </div>

          {task.result && (
            <div className="modal-section">
              <h4>Result</h4>
              <pre>{task.result}</pre>
            </div>
          )}
          {task.error && (
            <div className="modal-section modal-section--error">
              <h4>Error</h4>
              <pre>{task.error}</pre>
            </div>
          )}

          <div className="modal-section">
            <h4>Event log (audit trail)</h4>
            <div className="event-log">
              {task.eventLog.map((entry) => (
                <div className="event-line" key={entry.atMillis + entry.status}>
                  <span className="mono ev-time">{formatClock(entry.atMillis)}</span>
                  <span className="ev-status">{entry.status}</span>
                  <span className="ev-detail">{entry.detail}</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="modal-footer">
          {['PENDING', 'QUEUED', 'RUNNING'].includes(task.status) && (
            <button className="btn btn--danger" onClick={() => onCancel(task.id)}>
              Cancel task
            </button>
          )}
          {['FAILED', 'TIMED_OUT'].includes(task.status) && (
            <button className="btn btn--primary" onClick={() => onRetry(task.id)}>
              ⟳ Retry
            </button>
          )}
          <button className="btn btn--ghost" onClick={onClose}>Close</button>
        </div>
      </div>
    </div>
  );
}