import {
  createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode,
} from 'react'
import { api, setToken, setUnauthorizedHandler } from './api'
import type { Role, TokenResponse } from './types'

interface Session {
  token: string
  username: string
  role: Role
  expiresAt: number // epoch millis
}

interface AuthContextValue {
  session: Session | null
  login: (username: string, password: string) => Promise<void>
  register: (username: string, password: string) => Promise<void>
  logout: () => void
}

const STORAGE_KEY = 'incidentflow.session'
const AuthContext = createContext<AuthContextValue | null>(null)

function base64UrlDecode(part: string): string {
  const b64 = part.replace(/-/g, '+').replace(/_/g, '/')
  return atob(b64.padEnd(Math.ceil(b64.length / 4) * 4, '='))
}

// Reads the claims from the JWT payload. This is only for showing the right UI:
// the payload is not verified here, and the server enforces every permission.
function toSession(token: string): Session {
  const payload = JSON.parse(base64UrlDecode(token.split('.')[1])) as {
    sub: string
    roles: Role[]
    exp: number
  }
  return { token, username: payload.sub, role: payload.roles[0], expiresAt: payload.exp * 1000 }
}

function loadSession(): Session | null {
  const token = sessionStorage.getItem(STORAGE_KEY)
  if (!token) return null
  try {
    const session = toSession(token)
    return session.expiresAt > Date.now() ? session : null
  } catch {
    return null
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session | null>(() => {
    const restored = loadSession()
    setToken(restored?.token ?? null) // idempotent, so safe if React runs this twice
    return restored
  })

  const logout = useCallback(() => {
    sessionStorage.removeItem(STORAGE_KEY)
    setToken(null)
    setSession(null)
  }, [])

  const login = useCallback(async (username: string, password: string) => {
    const res = await api<TokenResponse>('/api/auth/login', {
      method: 'POST',
      body: { username, password },
    })
    sessionStorage.setItem(STORAGE_KEY, res.token)
    setToken(res.token)
    setSession(toSession(res.token))
  }, [])

  const register = useCallback(
    async (username: string, password: string) => {
      await api('/api/auth/register', { method: 'POST', body: { username, password } })
      await login(username, password)
    },
    [login],
  )

  useEffect(() => {
    setUnauthorizedHandler(logout)
  }, [logout])

  // Log out automatically at the moment the token expires.
  useEffect(() => {
    if (!session) return
    const timer = setTimeout(logout, Math.max(session.expiresAt - Date.now(), 0))
    return () => clearTimeout(timer)
  }, [session, logout])

  const value = useMemo(() => ({ session, login, register, logout }), [session, login, register, logout])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside <AuthProvider>')
  return ctx
}
