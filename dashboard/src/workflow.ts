import type { IncidentStatus, Role } from './types'

// Mirrors IncidentStatus.canTransitionTo on the server. The server is the authority:
// if these ever drift apart, the server rejects the request with a 409 and the UI says so.
export const NEXT_STATUS: Record<IncidentStatus, IncidentStatus | null> = {
  OPEN: 'ACKNOWLEDGED',
  ACKNOWLEDGED: 'RESOLVED',
  RESOLVED: null,
}

export const ACTION_LABEL: Record<IncidentStatus, string> = {
  OPEN: 'Reopen',
  ACKNOWLEDGED: 'Acknowledge',
  RESOLVED: 'Resolve',
}

// UI gating only. These match the server's rules, which are what actually protect the data.
export const canWrite = (role: Role) => role === 'ADMIN' || role === 'RESPONDER'
export const isAdmin = (role: Role) => role === 'ADMIN'

export const formatTime = (iso: string) => new Date(iso).toLocaleString()
