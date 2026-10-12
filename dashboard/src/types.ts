export type Role = 'ADMIN' | 'RESPONDER' | 'VIEWER'

export interface TokenResponse {
  token: string
  tokenType: string
  expiresInSeconds: number
}

export interface NotificationDto {
  id: number
  eventType: string
  incidentId: number
  serviceName: string
  severity: string
  message: string
  occurredAt: string
}

// Spring's ProblemDetail, plus the "errors" map our validation handler adds.
export interface ProblemDetail {
  status?: number
  title?: string
  detail?: string
  errors?: Record<string, string>
}

export type Severity = 'SEV1' | 'SEV2' | 'SEV3' | 'SEV4'
export type IncidentStatus = 'OPEN' | 'ACKNOWLEDGED' | 'RESOLVED'

export interface Incident {
  id: number
  title: string
  description: string | null
  severity: Severity
  status: IncidentStatus
  serviceId: number
  serviceName: string
  version: number
  createdAt: string
  updatedAt: string
  acknowledgedAt: string | null
  resolvedAt: string | null
}

export interface ServiceSummary {
  id: number
  name: string
  description: string | null
  ownerTeam: string | null
  createdAt: string
}

export interface AuditEntry {
  fromStatus: IncidentStatus | null
  toStatus: IncidentStatus
  note: string | null
  changedAt: string
}

// Shape of the PagedModel our incident list returns.
export interface Page<T> {
  content: T[]
  page: { size: number; number: number; totalElements: number; totalPages: number }
}
