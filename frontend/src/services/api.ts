import type { Activity } from '@/types/activity'

const API_BASE = 'http://localhost:8080/api'

export async function fetchActivities(): Promise<Activity[]> {
  const response = await fetch(`${API_BASE}/activities`)
  if (!response.ok) throw new Error('Failed to fetch activities')
  return response.json()
}

export async function searchActivities(params: {
  query?: string
  category?: string
  maxPrice?: number
  maxDuration?: number
}): Promise<Activity[]> {
  const searchParams = new URLSearchParams()
  if (params.query) searchParams.set('query', params.query)
  if (params.category) searchParams.set('category', params.category)
  if (params.maxPrice) searchParams.set('maxPrice', params.maxPrice.toString())
  if (params.maxDuration) searchParams.set('maxDuration', params.maxDuration.toString())

  const response = await fetch(`${API_BASE}/activities/search?${searchParams}`)
  if (!response.ok) throw new Error('Failed to search activities')
  return response.json()
}
