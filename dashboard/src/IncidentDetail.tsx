import { useCallback, useEffect, useState } from 'react'
import { ApiError } from './api'
import { describe } from './errors'
import { changeStatus, getHistory, getIncident } from './incidents'
import type { AuditEntry, Incident, IncidentStatus, Role } from './types'
import { ACTION_LABEL, canWrite, formatTime, NEXT_STATUS } from './workflow'

interface Props {
  id: number
  role: Role
  refreshTick: number
  onChanged: () => void
  onClose: () => void
}

export default function IncidentDetail({ id, role, refreshTick, onChanged, onClose }: Props) {
  const [incident, setIncident] = useState<Incident | null>(null)
  const [history, setHistory] = useState<AuditEntry[]>([])
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)
  const [note, setNote] = useState('')
  const [busy, setBusy] = useState(false)

  const load = useCallback(async () => {
    const [current, entries] = await Promise.all([getIncident(id), getHistory(id)])
    return { current, entries }
  }, [id])

  // Load on open, and again on every live event.
  useEffect(() => {
    let cancelled = false
    load()
      .then(({ current, entries }) => {
        if (cancelled) return
        setIncident(current)
        setHistory(entries)
        setError(null)
      })
      .catch((err) => !cancelled && setError(describe(err)))
    return () => {
      cancelled = true
    }
  }, [load, refreshTick])

  async function advance(next: IncidentStatus) {
    if (!incident) return
    setBusy(true)
    setError(null)
    setNotice(null)
    try {
      // The version we send is the one we are looking at. If someone changed the incident
      // since, the server rejects this with 409 instead of silently overwriting their change.
      const updated = await changeStatus(incident.id, {
        status: next,
        version: incident.version,
        note: note.trim() || undefined,
      })
      setIncident(updated)
      setNote('')
      setHistory(await getHistory(incident.id))
      onChanged()
    } catch (err) {
      if (err instanceof ApiError && err.status === 409) {
        setNotice(`${err.message} The latest data has been reloaded, so please review it and try again.`)
      } else {
        setError(describe(err))
      }
      try {
        const { current, entries } = await load()
        setIncident(current)
        setHistory(entries)
      } catch {
        // keep showing what we have
      }
    } finally {
      setBusy(false)
    }
  }

  const next = incident ? NEXT_STATUS[incident.status] : null

  return (
    <section className="panel">
      <div className="panel-head">
        <h2>Incident #{id}</h2>
        <button onClick={onClose}>Close</button>
      </div>

      {error && <p className="error" role="alert">{error}</p>}
      {notice && <p className="notice" role="status">{notice}</p>}
      {!incident && !error && <p className="muted">Loading…</p>}

      {incident && (
        <>
          <h3 className="title">{incident.title}</h3>
          <p className="badges">
            <span className={`badge sev-${incident.severity}`}>{incident.severity}</span>
            <span className={`badge st-${incident.status}`}>{incident.status}</span>
            <span className="muted">{incident.serviceName}</span>
            <span className="muted">· version {incident.version}</span>
          </p>
          {incident.description && <p className="desc">{incident.description}</p>}

          {canWrite(role) && next && (
            <div className="actions">
              <input
                placeholder="Optional note for the audit log"
                value={note}
                maxLength={500}
                onChange={(e) => setNote(e.target.value)}
              />
              <button type="button" className="primary" disabled={busy} onClick={() => advance(next)}>
                {busy ? 'Saving…' : ACTION_LABEL[next]}
              </button>
            </div>
          )}
          {canWrite(role) && !next && <p className="muted">This incident is resolved. No further changes are possible.</p>}
          {!canWrite(role) && <p className="muted">Your role can view incidents but not change them.</p>}

          <h3>History</h3>
          <ol className="timeline">
            {history.map((h, index) => (
              <li key={index}>
                <strong>{h.fromStatus ? `${h.fromStatus} → ${h.toStatus}` : h.toStatus}</strong>
                <time>{formatTime(h.changedAt)}</time>
                {h.note && <p>{h.note}</p>}
              </li>
            ))}
          </ol>
        </>
      )}
    </section>
  )
}
