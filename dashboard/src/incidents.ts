import { api } from './api'
import type { AuditEntry, Incident, IncidentStatus, Page, ServiceSummary, Severity } from './types'

export interface IncidentFilters {
  status?: IncidentStatus
  serviceId?: number
  page: number
  size: number
}

export function listIncidents(filters: IncidentFilters) {
  const query = new URLSearchParams({ page: String(filters.page), size: String(filters.size) })
  if (filters.status) query.set('status', filters.status)
  if (filters.serviceId) query.set('serviceId', String(filters.serviceId))
  return api<Page<Incident>>(`/api/incidents?${query}`)
}

export const getIncident = (id: number) => api<Incident>(`/api/incidents/${id}`)
export const getHistory = (id: number) => api<AuditEntry[]>(`/api/incidents/${id}/history`)

export const createIncident = (body: {
  title: string
  description?: string
  severity: Severity
  serviceId: number
}) => api<Incident>('/api/incidents', { method: 'POST', body })

export const changeStatus = (
  id: number,
  body: { status: IncidentStatus; version: number; note?: string },
) => api<Incident>(`/api/incidents/${id}/status`, { method: 'PATCH', body })

export const listServices = () => api<ServiceSummary[]>('/api/services')

export const createService = (body: { name: string; description?: string; ownerTeam?: string }) =>
  api<ServiceSummary>('/api/services', { method: 'POST', body })
