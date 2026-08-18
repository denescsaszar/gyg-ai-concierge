<script setup lang="ts">
import { computed } from 'vue'
import type { Activity } from '@/types/activity'

const props = withDefaults(
  defineProps<{
    activity: Activity
    recommended?: boolean
    /** Above-the-fold cards load eagerly; everything else is deferred. */
    priority?: boolean
  }>(),
  { recommended: false, priority: false },
)

const CARD_WIDTH = 400
const CARD_HEIGHT = 250

/**
 * Unsplash serves through imgix, so `auto=format` gives us AVIF/WebP where the
 * browser supports it and `q=70` roughly halves the bytes at no visible cost.
 * srcset covers 1x and 2x displays instead of always shipping the larger file.
 */
function unsplashUrl(base: string, width: number, height: number): string {
  return `${base}?w=${width}&h=${height}&fit=crop&auto=format&q=70`
}

const imageSrc = computed(() => unsplashUrl(props.activity.imageUrl, CARD_WIDTH, CARD_HEIGHT))

const imageSrcset = computed(
  () =>
    `${unsplashUrl(props.activity.imageUrl, CARD_WIDTH, CARD_HEIGHT)} 1x, ` +
    `${unsplashUrl(props.activity.imageUrl, CARD_WIDTH * 2, CARD_HEIGHT * 2)} 2x`,
)

const categoryLabel = computed(() => props.activity.category.split('_').join(' ').toLowerCase())

const durationLabel = computed(() => {
  const hours = Math.floor(props.activity.durationMinutes / 60)
  const mins = props.activity.durationMinutes % 60
  if (hours === 0) return `${mins}min`
  if (mins === 0) return `${hours}h`
  return `${hours}h ${mins}min`
})

const priceLabel = computed(() =>
  props.activity.priceEur === 0 ? 'Free' : `€${props.activity.priceEur.toFixed(0)}`,
)
</script>

<template>
  <article class="card" :class="{ 'card-recommended': recommended }">
    <div class="card-image">
      <img
        :src="imageSrc"
        :srcset="imageSrcset"
        :alt="`${activity.title} in ${activity.city}`"
        :width="CARD_WIDTH"
        :height="CARD_HEIGHT"
        :loading="priority ? 'eager' : 'lazy'"
        :fetchpriority="priority ? 'high' : 'auto'"
        decoding="async"
      />
      <span class="category-badge">{{ categoryLabel }}</span>
      <span v-if="recommended" class="recommended-badge">AI Recommended</span>
    </div>
    <div class="card-content">
      <h3>{{ activity.title }}</h3>
      <p class="description">{{ activity.description }}</p>
      <ul class="highlights">
        <li v-for="highlight in activity.highlights.slice(0, 3)" :key="highlight" class="highlight">
          {{ highlight }}
        </li>
      </ul>
      <div class="card-footer">
        <p class="rating">
          <span class="star" aria-hidden="true">&#9733;</span>
          <span>
            <span class="sr-only">Rated</span>{{ activity.rating }}
            <span class="sr-only">out of 5 from</span>
          </span>
          <span class="review-count">({{ activity.reviewCount.toLocaleString() }})</span>
          <span class="sr-only">reviews</span>
        </p>
        <p class="meta">
          <span class="duration">{{ durationLabel }}</span>
          <span class="price">{{ priceLabel }}</span>
        </p>
      </div>
    </div>
  </article>
</template>

<style scoped>
.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition:
    transform 0.2s,
    box-shadow 0.2s;
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.card-recommended {
  outline: 2px solid #ff5533;
  outline-offset: -2px;
}

.card-image {
  position: relative;
  height: 200px;
  overflow: hidden;
  background: #eceff1;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.category-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(0, 0, 0, 0.72);
  color: white;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 500;
  text-transform: capitalize;
}

.card-content {
  padding: 16px;
}

.card-content h3 {
  font-size: 1.05rem;
  font-weight: 600;
  margin-bottom: 8px;
  line-height: 1.3;
}

.description {
  font-size: 0.85rem;
  color: #595959;
  line-height: 1.5;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.highlights {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 14px;
  list-style: none;
  padding: 0;
}

.highlight {
  background: #f0f0f0;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 0.75rem;
  color: #4a4a4a;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.rating {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.9rem;
  font-weight: 500;
}

.star {
  color: #d13b1c;
}

.review-count {
  color: #6b6b6b;
  font-weight: 400;
  font-size: 0.8rem;
}

.meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.duration {
  font-size: 0.85rem;
  color: #595959;
}

.price {
  font-size: 1.1rem;
  font-weight: 700;
  color: #d13b1c;
}

.recommended-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: #ff5533;
  color: white;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
}

@media (prefers-reduced-motion: reduce) {
  .card,
  .card:hover {
    transition: none;
    transform: none;
  }
}
</style>
