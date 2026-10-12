import type { StreamStatus } from './useNotifications'
import type { NotificationDto } from './types'

interface Props {
  items: NotificationDto[]
  status: StreamStatus
  onSelect: (incidentId: number) => void
}

export default function NotificationFeed({ items, status, onSelect }: Props) {
  return (
    <section className="panel">
      <h2>
        Live notifications <span className={`dot ${status}`} title={status} /> <small>{status}</small>
      </h2>
      {items.length === 0 && <p className="muted">Nothing yet. Create or change an incident to see it here.</p>}
      <ul className="feed">
        {items.map((n) => (
          <li key={n.id}>
            <span className={`badge sev-${n.severity}`}>{n.severity}</span>
            <button className="link-btn msg" onClick={() => onSelect(n.incidentId)}>{n.message}</button>
            <time>{new Date(n.occurredAt).toLocaleTimeString()}</time>
          </li>
        ))}
      </ul>
    </section>
  )
}
