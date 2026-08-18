<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import type { Activity } from '@/types/activity'
import { fetchActivities } from '@/services/api'
import { FALLBACK_ACTIVITIES } from '@/data/activities'
import ActivityCard from '@/components/ActivityCard.vue'
import ChatWidget from '@/components/ChatWidget.vue'

type CatalogSource = 'snapshot' | 'live' | 'offline'

// Seeded from the bundled snapshot so the grid paints on first frame instead
// of waiting out a Render cold start. Replaced by live data once it arrives.
const catalog = ref<Activity[]>(FALLBACK_ACTIVITIES)
const source = ref<CatalogSource>('snapshot')
const searchQuery = ref('')
const recommendedIds = ref<number[]>([])

const controller = new AbortController()

const visibleActivities = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()

  // 10 items already in memory — a network round-trip per keystroke would be
  // strictly slower than filtering here.
  const matches = q
    ? catalog.value.filter((a) =>
        [a.title, a.description, a.city, a.category.replace('_', ' '), ...a.highlights]
          .join(' ')
          .toLowerCase()
          .includes(q),
      )
    : catalog.value

  if (recommendedIds.value.length === 0) return matches

  // Rank by the order the concierge returned them, then everything else.
  const rank = new Map(recommendedIds.value.map((id, i) => [id, i]))
  return [...matches].sort(
    (a, b) => (rank.get(a.id) ?? Number.MAX_SAFE_INTEGER) - (rank.get(b.id) ?? Number.MAX_SAFE_INTEGER),
  )
})

async function refreshCatalog() {
  try {
    const live = await fetchActivities(controller.signal)
    if (live.length > 0) {
      catalog.value = live
      source.value = 'live'
    }
  } catch (e) {
    if (controller.signal.aborted) return
    source.value = 'offline'
    console.warn('Live catalog unavailable, showing bundled snapshot:', e)
  }
}

function handleRecommendations(activityIds: number[]) {
  recommendedIds.value = activityIds
}

function clearSearch() {
  searchQuery.value = ''
}

onMounted(refreshCatalog)
onUnmounted(() => controller.abort())
</script>

<template>
  <main class="home">
    <section class="hero">
      <h1>AI Travel Concierge</h1>
      <p>Describe your perfect trip, and let AI find the best experiences for you.</p>
      <div class="search-bar" role="search">
        <label class="sr-only" for="activity-search">Search Berlin experiences</label>
        <input
          id="activity-search"
          v-model="searchQuery"
          type="search"
          autocomplete="off"
          placeholder="Search activities... (e.g. food, museum, outdoor)"
        />
        <button type="button" @click="clearSearch" :disabled="!searchQuery">Clear</button>
      </div>
    </section>

    <section class="activities" aria-labelledby="results-heading">
      <h2 id="results-heading" class="sr-only">Berlin experiences</h2>

      <p class="results-meta" role="status" aria-live="polite">
        {{ visibleActivities.length }}
        {{ visibleActivities.length === 1 ? 'experience' : 'experiences' }}
        <span v-if="source === 'snapshot'" class="badge badge-waking">syncing with server…</span>
        <span v-else-if="source === 'offline'" class="badge badge-offline">
          showing the built-in catalog — the API is asleep or unreachable
        </span>
      </p>

      <div v-if="visibleActivities.length === 0" class="empty">
        No activities match “{{ searchQuery }}”. Try a different search.
      </div>
      <div v-else class="activity-grid">
        <ActivityCard
          v-for="(activity, index) in visibleActivities"
          :key="activity.id"
          :activity="activity"
          :recommended="recommendedIds.includes(activity.id)"
          :priority="index < 3"
        />
      </div>
    </section>

    <ChatWidget @recommendations="handleRecommendations" />
  </main>
</template>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.5rem;
}

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

.hero {
  text-align: center;
  padding: 3rem 0 2rem;
}

.hero h1 {
  font-size: 2.5rem;
  font-weight: 700;
  color: #d13b1c;
  margin-bottom: 0.75rem;
}

.hero p {
  font-size: 1.15rem;
  color: #595959;
  margin-bottom: 2rem;
}

.search-bar {
  display: flex;
  max-width: 600px;
  margin: 0 auto;
  gap: 8px;
}

.search-bar input {
  flex: 1;
  padding: 14px 18px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  font-size: 1rem;
  outline: none;
  transition: border-color 0.2s;
}

.search-bar input:focus-visible {
  border-color: #ff5533;
  outline: 2px solid #d13b1c;
  outline-offset: 2px;
}

.search-bar button {
  padding: 14px 28px;
  background: #ff5533;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.search-bar button:hover:not(:disabled) {
  background: #e64a2e;
}

.search-bar button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.activities {
  padding: 1rem 0 4rem;
}

.results-meta {
  font-size: 0.85rem;
  color: #595959;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.badge {
  font-size: 0.75rem;
  padding: 2px 10px;
  border-radius: 20px;
}

.badge-waking {
  background: #fff1ec;
  color: #b8340f;
}

.badge-offline {
  background: #f2f2f2;
  color: #595959;
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.empty {
  text-align: center;
  padding: 3rem;
  color: #595959;
  font-size: 1.1rem;
}

@media (max-width: 768px) {
  .hero h1 {
    font-size: 1.75rem;
  }

  .hero p {
    font-size: 1rem;
  }

  .search-bar {
    flex-direction: column;
  }

  .activity-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}
</style>
