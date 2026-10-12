import { useCallback, useEffect, useRef, useState } from 'react'
import { AuthProvider, useAuth } from './auth'
import IncidentDetail from './IncidentDetail'
import IncidentList from './IncidentList'
import { listServices } from './incidents'
import LoginPage from './LoginPage'
import NotificationFeed from './NotificationFeed'
import ServicesPanel from './ServicesPanel'
import type { ServiceSummary } from './types'
import { useNotifications } from './useNotifications'
import { canWrite, isAdmin } from './workflow'

function Dashboard() {
  const { session, logout } = useAuth()
  const [services, setServices] = useState<ServiceSummary[]>([])
  const [selectedId, setSelectedId] = useState<number | null>(null)

  // Bumping this number makes the list and the open incident refetch themselves.
  const [tick, setTick] = useState(0)
  const refreshNow = useCallback(() => setTick((t) => t + 1), [])

  // Live events can arrive in bursts, so wait briefly and refresh once.
  const timer = useRef<number | undefined>(undefined)
  const refreshSoon = useCallback(() => {
    window.clearTimeout(timer.current)
    timer.current = window.setTimeout(refreshNow, 300)
  }, [refreshNow])
  useEffect(() => () => window.clearTimeout(timer.current), [])

  const { items, status } = useNotifications(session?.token ?? null, refreshSoon)

  const loadServices = useCallback(() => {
    listServices().then(setServices).catch(() => {})
  }, [])
  useEffect(loadServices, [loadServices])

  if (!session) return null

  return (
    <div className="shell">
      <header>
        <h1>IncidentFlow</h1>
        <div className="who">
          <span className={`badge role-${session.role}`}>{session.role}</span>
          <span>{session.username}</span>
          <button onClick={logout}>Log out</button>
        </div>
      </header>

      <div className="layout">
        <div className="col">
          <IncidentList
            services={services}
            selectedId={selectedId}
            refreshTick={tick}
            canCreate={canWrite(session.role)}
            onSelect={setSelectedId}
            onChanged={refreshNow}
          />
          <NotificationFeed items={items} status={status} onSelect={setSelectedId} />
        </div>

        <div className="col">
          {selectedId !== null && (
            <IncidentDetail
              key={selectedId}
              id={selectedId}
              role={session.role}
              refreshTick={tick}
              onChanged={refreshNow}
              onClose={() => setSelectedId(null)}
            />
          )}
          <ServicesPanel services={services} canCreate={isAdmin(session.role)} onCreated={loadServices} />
        </div>
      </div>
    </div>
  )
}

function Gate() {
  const { session } = useAuth()
  return session ? <Dashboard /> : <LoginPage />
}

export default function App() {
  return (
    <AuthProvider>
      <Gate />
    </AuthProvider>
  )
}
