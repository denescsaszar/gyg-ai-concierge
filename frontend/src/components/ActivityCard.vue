<script setup lang="ts">
import type { Activity } from '@/types/activity'

defineProps<{
  activity: Activity
}>()

function formatDuration(minutes: number): string {
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours === 0) return `${mins}min`
  if (mins === 0) return `${hours}h`
  return `${hours}h ${mins}min`
}

function formatPrice(price: number): string {
  if (price === 0) return 'Free'
  return `€${price.toFixed(0)}`
}
</script>

<template>
  <article class="card">
    <div class="card-image">
      <img :src="activity.imageUrl + '?w=400&h=250&fit=crop'" :alt="activity.title" />
      <span class="category-badge">{{ activity.category.replace('_', ' ') }}</span>
    </div>
    <div class="card-content">
      <h3>{{ activity.title }}</h3>
      <p class="description">{{ activity.description }}</p>
      <div class="highlights">
        <span
          v-for="highlight in activity.highlights.slice(0, 3)"
          :key="highlight"
          class="highlight"
        >
          {{ highlight }}
        </span>
      </div>
      <div class="card-footer">
        <div class="rating">
          <span class="star">&#9733;</span>
          <span>{{ activity.rating }}</span>
          <span class="review-count">({{ activity.reviewCount.toLocaleString() }})</span>
        </div>
        <div class="meta">
          <span class="duration">{{ formatDuration(activity.durationMinutes) }}</span>
          <span class="price">{{ formatPrice(activity.priceEur) }}</span>
        </div>
      </div>
    </div>
  </article>
</template>

<style scoped>
.card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition:
    transform 0.2s,
    box-shadow 0.2s;
  cursor: pointer;
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.card-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.category-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(0, 0, 0, 0.6);
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
  color: #666;
  line-height: 1.5;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.highlights {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 14px;
}

.highlight {
  background: #f0f0f0;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 0.75rem;
  color: #555;
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
  color: #ff5533;
}

.review-count {
  color: #999;
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
  color: #666;
}

.price {
  font-size: 1.1rem;
  font-weight: 700;
  color: #ff5533;
}
</style>
