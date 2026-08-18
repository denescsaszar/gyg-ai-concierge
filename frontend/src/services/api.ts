import type { Activity, ChatMessage, ChatResponse } from '@/types/activity'

const API_BASE = import.meta.env.VITE_API_URL || '/api'

/**
 * The backend sleeps on Render's free tier and can take ~60s to wake.
 * Both budgets are generous enough to survive a cold start but bounded, so a
 * dead backend surfaces as an error instead of an infinite spinner.
 */
const CATALOG_TIMEOUT_MS = 90_000
const CHAT_TIMEOUT_MS = 120_000

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status?: number,
    readonly timedOut = false,
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

async function request<T>(path: string, init: RequestInit = {}, timeoutMs: number): Promise<T> {
  // AbortSignal.any lets a caller-supplied signal (e.g. component unmount)
  // cancel the request without discarding the timeout.
  const timeout = AbortSignal.timeout(timeoutMs)
  const signal = init.signal ? AbortSignal.any([init.signal, timeout]) : timeout

  let response: Response
  try {
    response = await fetch(`${API_BASE}${path}`, { ...init, signal })
  } catch (e) {
    if (timeout.aborted) {
      throw new ApiError(`Request to ${path} timed out after ${timeoutMs}ms`, undefined, true)
    }
    throw e
  }

  if (!response.ok) {
    throw new ApiError(`Request to ${path} failed`, response.status)
  }
  return response.json() as Promise<T>
}

export function fetchActivities(signal?: AbortSignal): Promise<Activity[]> {
  return request<Activity[]>('/activities', { signal }, CATALOG_TIMEOUT_MS)
}

export function searchActivities(
  params: {
    query?: string
    category?: string
    maxPrice?: number
    maxDuration?: number
  },
  signal?: AbortSignal,
): Promise<Activity[]> {
  const searchParams = new URLSearchParams()
  if (params.query) searchParams.set('query', params.query)
  if (params.category) searchParams.set('category', params.category)
  if (params.maxPrice !== undefined) searchParams.set('maxPrice', String(params.maxPrice))
  if (params.maxDuration !== undefined) searchParams.set('maxDuration', String(params.maxDuration))

  return request<Activity[]>(`/activities/search?${searchParams}`, { signal }, CATALOG_TIMEOUT_MS)
}

export function sendChatMessage(
  message: string,
  conversationHistory: ChatMessage[],
  signal?: AbortSignal,
): Promise<ChatResponse> {
  return request<ChatResponse>(
    '/concierge/chat',
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message, conversationHistory }),
      signal,
    },
    CHAT_TIMEOUT_MS,
  )
}
