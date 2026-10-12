import { useEffect, useRef, useState } from 'react'
import { EventStreamContentType, fetchEventSource } from '@microsoft/fetch-event-source'
import { api } from './api'
import type { NotificationDto } from './types'

export type StreamStatus = 'connecting' | 'live' | 'reconnecting'

class FatalError extends Error {}

// Newest first, no duplicates (replay after a reconnect can resend an id), capped at 100.
function merge(current: NotificationDto[], incoming: NotificationDto[]): NotificationDto[] {
  const byId = new Map<number, NotificationDto>()
  for (const n of [...incoming, ...current]) byId.set(n.id, n)
  return [...byId.values()].sort((a, b) => b.id - a.id).slice(0, 100)
}

export function useNotifications(token: string | null, onNew?: (n: NotificationDto) => void) {
  const [items, setItems] = useState<NotificationDto[]>([])
  const [status, setStatus] = useState<StreamStatus>('connecting')

  // Keep the latest callback without restarting the stream whenever it changes.
  const onNewRef = useRef(onNew)
  useEffect(() => {
    onNewRef.current = onNew
  })

  useEffect(() => {
    if (!token) return
    const controller = new AbortController()

    async function start() {
      setStatus('connecting')

      // 1. Load recent history, remembering the newest id we have.
      let lastId = 0
      try {
        const latest = await api<NotificationDto[]>('/api/notifications')
        if (controller.signal.aborted) return
        lastId = latest.reduce((max, n) => Math.max(max, n.id), 0)
        setItems((prev) => merge(prev, latest))
      } catch {
        // An expired token already triggered logout inside api(); the stream below retries otherwise.
      }

      // 2. Open the live stream. Passing the newest id asks the server to replay anything
      //    that arrived between the history request and now, so nothing is missed.
      //    The header name is lowercase on purpose: the library sets 'last-event-id' itself on
      //    every automatic reconnect, and a different casing would send two conflicting values.
      await fetchEventSource('/api/notifications/stream', {
        signal: controller.signal,
        openWhenHidden: true,
        headers: {
          Authorization: `Bearer ${token}`,
          ...(lastId > 0 ? { 'last-event-id': String(lastId) } : {}),
        },
        async onopen(res) {
          if (res.ok && res.headers.get('content-type')?.startsWith(EventStreamContentType)) {
            setStatus('live')
            return
          }
          if (res.status === 401) throw new FatalError('unauthorized')
          throw new Error(`Unexpected response ${res.status}`)
        },
        onmessage(ev) {
          if (ev.event !== 'notification' || !ev.data) return
          const n = JSON.parse(ev.data) as NotificationDto
          setItems((prev) => merge(prev, [n]))
          onNewRef.current?.(n)
        },
        onclose() {
          throw new Error('stream closed by server') // makes the library reconnect
        },
        onerror(err) {
          if (err instanceof FatalError) throw err // stop retrying
          setStatus('reconnecting')
          return 2000 // retry in 2 seconds
        },
      })
    }

    start().catch(() => {
      // FatalError (rejected token) ends up here; nothing more to do.
    })
    return () => controller.abort()
  }, [token])

  return { items, status }
}
