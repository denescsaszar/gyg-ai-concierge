import { Category, type Activity } from '@/types/activity'

/**
 * Build-time snapshot of the backend catalog (`SampleActivities.kt`).
 *
 * Why this exists: the backend runs on Render's free tier, which spins the
 * service down after 15 minutes of inactivity and takes ~1 minute to wake up.
 * Blocking the first paint on that request meant the first visitor after a
 * quiet period saw a spinner for the better part of a minute.
 *
 * The grid now renders from this snapshot immediately, and `HomeView` refreshes
 * it from `/api/activities` in the background. The API stays the source of
 * truth — this is just what we show while it wakes up.
 *
 * Keep in sync with backend/src/main/kotlin/com/gyg/concierge/data/SampleActivities.kt
 */
export const FALLBACK_ACTIVITIES: Activity[] = [
  {
    id: 1,
    title: 'Berlin Wall & Cold War Walking Tour',
    description:
      'Explore the history of divided Berlin with an expert local guide. Visit Checkpoint Charlie, the East Side Gallery, and hidden remnants of the Wall.',
    city: 'Berlin',
    category: Category.TOURS,
    priceEur: 18.0,
    durationMinutes: 150,
    rating: 4.8,
    reviewCount: 2341,
    imageUrl: 'https://images.unsplash.com/photo-1560969184-10fe8719e047',
    highlights: ['Small group (max 15)', 'Licensed guide', 'Checkpoint Charlie', 'East Side Gallery'],
    isBookable: true,
  },
  {
    id: 2,
    title: 'Berlin Street Food Tour: Kreuzberg Flavors',
    description:
      "Taste your way through Berlin's most diverse neighborhood. Sample döner, currywurst, Turkish bakeries, and craft beer.",
    city: 'Berlin',
    category: Category.FOOD_AND_DRINK,
    priceEur: 45.0,
    durationMinutes: 180,
    rating: 4.9,
    reviewCount: 876,
    imageUrl: 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38',
    highlights: ['6 food stops', 'Drinks included', 'Local favorites', 'Vegetarian options'],
    isBookable: true,
  },
  {
    id: 3,
    title: 'Museum Island: Pergamon & Neues Museum',
    description:
      'Skip the line and discover world-class antiquities including the Ishtar Gate and the bust of Nefertiti with an art historian guide.',
    city: 'Berlin',
    category: Category.MUSEUMS,
    priceEur: 35.0,
    durationMinutes: 120,
    rating: 4.7,
    reviewCount: 1543,
    imageUrl: 'https://images.unsplash.com/photo-1584551246679-0daf3d275d0f',
    highlights: ['Skip-the-line entry', 'Expert art historian', 'Pergamon Altar', 'Nefertiti bust'],
    isBookable: true,
  },
  {
    id: 4,
    title: 'Kayak Tour on the Spree River',
    description:
      "Paddle through Berlin's heart and see the Reichstag, Cathedral, and Museum Island from a unique perspective on the water.",
    city: 'Berlin',
    category: Category.OUTDOOR,
    priceEur: 39.0,
    durationMinutes: 150,
    rating: 4.9,
    reviewCount: 432,
    imageUrl: 'https://images.unsplash.com/photo-1472745942893-4b9f730c7668',
    highlights: ['All equipment provided', 'No experience needed', 'Small groups', 'Photo opportunities'],
    isBookable: true,
  },
  {
    id: 5,
    title: 'Reichstag Building: Rooftop & Dome Visit',
    description:
      "Visit the iconic glass dome of the German parliament with a guided audio tour explaining Berlin's political history.",
    city: 'Berlin',
    category: Category.TOURS,
    priceEur: 0.0,
    durationMinutes: 60,
    rating: 4.6,
    reviewCount: 5621,
    imageUrl: 'https://images.unsplash.com/photo-1551634979-2b11f8c946fe',
    highlights: ['Free entry', 'Audio guide included', 'Panoramic views', 'Advance booking required'],
    isBookable: true,
  },
  {
    id: 6,
    title: 'Berlin Craft Beer & Brewery Tour',
    description:
      "Discover Berlin's booming craft beer scene with visits to three local microbreweries and tastings of 10+ beers.",
    city: 'Berlin',
    category: Category.NIGHTLIFE,
    priceEur: 55.0,
    durationMinutes: 210,
    rating: 4.8,
    reviewCount: 298,
    imageUrl: 'https://images.unsplash.com/photo-1535958636474-b021ee887b13',
    highlights: ['3 brewery visits', '10+ beer tastings', 'Snacks included', '18+ only'],
    isBookable: true,
  },
  {
    id: 7,
    title: 'Family Bike Tour: Parks & Playgrounds',
    description:
      'A kid-friendly cycling tour through Tiergarten and along the canal, with stops at playgrounds and an ice cream break.',
    city: 'Berlin',
    category: Category.FAMILY,
    priceEur: 29.0,
    durationMinutes: 120,
    rating: 4.9,
    reviewCount: 187,
    imageUrl: 'https://images.unsplash.com/photo-1571068316344-75bc76f77890',
    highlights: ['Child bikes & seats available', 'Ice cream included', 'Flat route', 'Ages 4+'],
    isBookable: true,
  },
  {
    id: 8,
    title: 'Urban Climbing: Indoor Bouldering Experience',
    description:
      "Challenge yourself at Berlin's top bouldering gym with a beginner-friendly intro session and full equipment rental.",
    city: 'Berlin',
    category: Category.ADVENTURE,
    priceEur: 25.0,
    durationMinutes: 90,
    rating: 4.7,
    reviewCount: 156,
    imageUrl: 'https://images.unsplash.com/photo-1522163182402-834f871fd851',
    highlights: ['All levels welcome', 'Equipment included', 'Professional instructor', 'No booking fee'],
    isBookable: true,
  },
  {
    id: 9,
    title: 'Currywurst Workshop: Make Your Own',
    description:
      "Learn the secrets of Berlin's iconic street food. Mix your own curry sauce, grill the perfect wurst, and eat your creation.",
    city: 'Berlin',
    category: Category.WORKSHOPS,
    priceEur: 42.0,
    durationMinutes: 90,
    rating: 4.8,
    reviewCount: 223,
    imageUrl: 'https://images.unsplash.com/photo-1432139555190-58524dae6a55',
    highlights: ['Hands-on cooking', 'Recipe to take home', 'All ingredients provided', 'Vegetarian option'],
    isBookable: true,
  },
  {
    id: 10,
    title: 'Sachsenhausen Concentration Camp Memorial Tour',
    description:
      'A respectful, in-depth guided visit to the Sachsenhausen memorial site with expert historical context and round-trip transport.',
    city: 'Berlin',
    category: Category.TOURS,
    priceEur: 22.0,
    durationMinutes: 360,
    rating: 4.9,
    reviewCount: 3102,
    imageUrl: 'https://images.unsplash.com/photo-1577084381621-7e04d24b6b43',
    highlights: ['Licensed memorial guide', 'Transport included', 'Small group', '6-hour deep dive'],
    isBookable: true,
  },
]
