import { ApiError } from './api'

// Turns any thrown value into a message that is safe to show to a user.
export function describe(err: unknown): string {
  if (err instanceof ApiError) {
    const fields = err.problem.errors
    if (fields) return Object.entries(fields).map(([f, m]) => `${f}: ${m}`).join(' · ')
    return err.message
  }
  return 'Could not reach the server'
}
