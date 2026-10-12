import type { ProblemDetail } from './types'

export class ApiError extends Error {
  status: number
  problem: ProblemDetail

  constructor(status: number, problem: ProblemDetail) {
    super(problem.detail ?? problem.title ?? `Request failed (${status})`)
    this.status = status
    this.problem = problem
  }
}

// The token lives in module state so every call can attach it without prop drilling.
let token: string | null = null
let onUnauthorized: () => void = () => {}

export function setToken(value: string | null) {
  token = value
}

export function setUnauthorizedHandler(handler: () => void) {
  onUnauthorized = handler
}

interface Options {
  method?: string
  body?: unknown
}

export async function api<T>(path: string, { method = 'GET', body }: Options = {}): Promise<T> {
  const headers: Record<string, string> = {}
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  if (token) headers.Authorization = `Bearer ${token}`

  const res = await fetch(path, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  })

  if (!res.ok) {
    // A 401 while holding a token means it expired or was rejected: drop the session.
    // A 401 on the login form (no token yet) just means wrong credentials.
    if (res.status === 401 && token) onUnauthorized()
    throw new ApiError(res.status, await readProblem(res))
  }
  if (res.status === 204) return undefined as T
  return (await res.json()) as T
}

async function readProblem(res: Response): Promise<ProblemDetail> {
  try {
    return (await res.json()) as ProblemDetail
  } catch {
    return { status: res.status }
  }
}
