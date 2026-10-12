import { useEffect, useState } from 'react'
import CreateIncidentForm from './CreateIncidentForm'
import { describe } from './errors'
import { listIncidents } from './incidents'
import type { Incident, IncidentStatus, Page, ServiceSummary } from './types'
import { formatTime } from './workflow'

const PAGE_SIZE = 10

interface Props {
  services: ServiceSummary[]
  selectedId: number | null
  refreshTick: number
  canCreate: boolean
  onSelect: (id: number) => void
  onChanged: () => void
}

export default function IncidentList({ services, selectedId, refreshTick, canCreate, onSelect, onChanged }: Props) {
  const [status, setStatus] = useState<IncidentStatus | ''>('')
  const [serviceId, setServiceId] = useState<number | ''>('')
  const [page, setPage] = useState(0)
  const [data, setData] = useState<Page<Incident> | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)

  // Refetch whenever a filter, the page, or the refresh tick (a live event) changes.
  // The "cancelled" flag drops answers from requests that were superseded meanwhile.
  useEffect(() => {
    let cancelled = false
    setLoading(true)
    listIncidents({ status: status || undefined, serviceId: serviceId || undefined, page, size: PAGE_SIZE })
      .then((result) => {
        if (cancelled) return
        setData(result)
        setError(null)
      })
      .catch((err) => !cancelled && setError(describe(err)))
      .finally(() => !cancelled && setLoading(false))
    return () => {
      cancelled = true
    }
  }, [status, serviceId, page, refreshTick])

  const totalPages = data?.page.totalPages ?? 0

  return (
    <section className="panel">
      <div className="panel-head">
        <h2>Incidents</h2>
        {canCreate && (
          <button onClick={() => setShowForm((v) => !v)}>{showForm ? 'Cancel' : 'New incident'}</button>
        )}
      </div>

      {showForm && (
        <CreateIncidentForm
          services={services}
          onCreated={(incident) => {
            setShowForm(false)
            setPage(0)
            onSelect(incident.id)
            onChanged()
          }}
        />
      )}

      <div className="filters">
        <label>
          Status
          <select
            value={status}
            onChange={(e) => {
              setStatus(e.target.value as IncidentStatus | '')
              setPage(0)
            }}
          >
            <option value="">All</option>
            <option value="OPEN">Open</option>
            <option value="ACKNOWLEDGED">Acknowledged</option>
            <option value="RESOLVED">Resolved</option>
          </select>
        </label>
        <label>
          Service
          <select
            value={serviceId}
            onChange={(e) => {
              setServiceId(e.target.value ? Number(e.target.value) : '')
              setPage(0)
            }}
          >
            <option value="">All</option>
            {services.map((s) => (
              <option key={s.id} value={s.id}>{s.name}</option>
            ))}
          </select>
        </label>
        {loading && <span className="muted">Loading…</span>}
      </div>

      {error && <p className="error" role="alert">{error}</p>}

      {data && data.content.length === 0 && <p className="muted">No incidents match these filters.</p>}

      {data && data.content.length > 0 && (
        <table className="table">
          <thead>
            <tr><th>Sev</th><th>Title</th><th>Service</th><th>Status</th><th>Created</th></tr>
          </thead>
          <tbody>
            {data.content.map((i) => (
              <tr key={i.id} className={i.id === selectedId ? 'selected' : ''} onClick={() => onSelect(i.id)}>
                <td><span className={`badge sev-${i.severity}`}>{i.severity}</span></td>
                <td>
                  <button className="link-btn" onClick={(e) => { e.stopPropagation(); onSelect(i.id) }}>
                    #{i.id} {i.title}
                  </button>
                </td>
                <td>{i.serviceName}</td>
                <td><span className={`badge st-${i.status}`}>{i.status}</span></td>
                <td className="muted">{formatTime(i.createdAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {data && totalPages > 0 && (
        <div className="pager">
          <button disabled={page === 0} onClick={() => setPage((p) => p - 1)}>Previous</button>
          <span>
            Page {data.page.number + 1} of {totalPages} · {data.page.totalElements} total
          </span>
          <button disabled={page + 1 >= totalPages} onClick={() => setPage((p) => p + 1)}>Next</button>
        </div>
      )}
    </section>
  )
}
