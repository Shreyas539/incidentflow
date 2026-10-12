import { useState, type FormEvent } from 'react'
import { describe } from './errors'
import { createService } from './incidents'
import type { ServiceSummary } from './types'

interface Props {
  services: ServiceSummary[]
  canCreate: boolean
  onCreated: () => void
}

export default function ServicesPanel({ services, canCreate, onCreated }: Props) {
  const [name, setName] = useState('')
  const [ownerTeam, setOwnerTeam] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  async function submit(e: FormEvent) {
    e.preventDefault()
    setBusy(true)
    setError(null)
    try {
      await createService({ name: name.trim(), ownerTeam: ownerTeam.trim() || undefined })
      setName('')
      setOwnerTeam('')
      onCreated()
    } catch (err) {
      setError(describe(err))
    } finally {
      setBusy(false)
    }
  }

  return (
    <section className="panel">
      <h2>Services</h2>
      {services.length === 0 && <p className="muted">No services yet.</p>}
      <ul className="plain">
        {services.map((s) => (
          <li key={s.id}>
            <strong>{s.name}</strong> {s.ownerTeam && <span className="muted">· {s.ownerTeam}</span>}
          </li>
        ))}
      </ul>

      {canCreate && (
        <form className="form" onSubmit={submit}>
          <div className="row">
            <label>
              Name
              <input value={name} onChange={(e) => setName(e.target.value)} maxLength={100} required />
            </label>
            <label>
              Owner team
              <input value={ownerTeam} onChange={(e) => setOwnerTeam(e.target.value)} maxLength={100} />
            </label>
          </div>
          {error && <p className="error" role="alert">{error}</p>}
          <button type="submit" disabled={busy}>{busy ? 'Adding…' : 'Add service'}</button>
        </form>
      )}
    </section>
  )
}
