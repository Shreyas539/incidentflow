import { useState, type FormEvent } from 'react'
import { describe } from './errors'
import { createIncident } from './incidents'
import type { Incident, ServiceSummary, Severity } from './types'

interface Props {
  services: ServiceSummary[]
  onCreated: (incident: Incident) => void
}

export default function CreateIncidentForm({ services, onCreated }: Props) {
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [severity, setSeverity] = useState<Severity>('SEV3')
  const [serviceId, setServiceId] = useState<number | ''>('')
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  if (services.length === 0) {
    return <p className="muted">There are no services yet. An admin needs to add one before incidents can be created.</p>
  }

  async function submit(e: FormEvent) {
    e.preventDefault()
    if (serviceId === '') return
    setBusy(true)
    setError(null)
    try {
      onCreated(
        await createIncident({
          title: title.trim(),
          description: description.trim() || undefined,
          severity,
          serviceId,
        }),
      )
    } catch (err) {
      setError(describe(err))
      setBusy(false)
    }
  }

  return (
    <form className="form" onSubmit={submit}>
      <label>
        Title
        <input value={title} onChange={(e) => setTitle(e.target.value)} maxLength={200} required />
      </label>
      <label>
        Description
        <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={3} />
      </label>
      <div className="row">
        <label>
          Severity
          <select value={severity} onChange={(e) => setSeverity(e.target.value as Severity)}>
            <option value="SEV1">SEV1 – critical</option>
            <option value="SEV2">SEV2 – major</option>
            <option value="SEV3">SEV3 – minor</option>
            <option value="SEV4">SEV4 – low</option>
          </select>
        </label>
        <label>
          Service
          <select value={serviceId} onChange={(e) => setServiceId(e.target.value ? Number(e.target.value) : '')} required>
            <option value="">Select…</option>
            {services.map((s) => (
              <option key={s.id} value={s.id}>{s.name}</option>
            ))}
          </select>
        </label>
      </div>
      {error && <p className="error" role="alert">{error}</p>}
      <button type="submit" disabled={busy}>{busy ? 'Creating…' : 'Create incident'}</button>
    </form>
  )
}
