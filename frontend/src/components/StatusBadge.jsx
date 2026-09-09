import { STATUS_META } from '../status.js';

export default function StatusBadge({ status, progress }) {
  const meta = STATUS_META[status] || {
    label: status,
    color: '#888',
    pulse: false,
  };
  const cls = meta.pulse ? 'badge badge--pulse' : 'badge';
  return (
    <span
      className={cls}
      style={{ background: meta.color + '1f', color: meta.color }}
      title={status}
    >
      <span
        className="dot"
        style={{ background: meta.color, boxShadow: `0 0 6px ${meta.color}` }}
      />
      {meta.label}
      {status === 'RUNNING' && progress != null ? ` ${progress}%` : ''}
    </span>
  );
}