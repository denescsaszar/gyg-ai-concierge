export interface Activity {
  id: number
  title: string
  description: string
  city: string
  category: Category
  priceEur: number
  durationMinutes: number
  rating: number
  reviewCount: number
  imageUrl: string
  highlights: string[]
  isBookable: boolean
}

export enum Category {
  TOURS = 'TOURS',
  FOOD_AND_DRINK = 'FOOD_AND_DRINK',
  MUSEUMS = 'MUSEUMS',
  OUTDOOR = 'OUTDOOR',
  WORKSHOPS = 'WORKSHOPS',
  NIGHTLIFE = 'NIGHTLIFE',
  FAMILY = 'FAMILY',
  ADVENTURE = 'ADVENTURE',
}

export interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
}

export interface ChatResponse {
  message: string
  recommendedActivityIds: number[]
}
