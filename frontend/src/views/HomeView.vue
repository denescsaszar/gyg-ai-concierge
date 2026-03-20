<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { Activity } from '@/types/activity'
import { fetchActivities, searchActivities } from '@/services/api'
import ActivityCard from '@/components/ActivityCard.vue'
import ChatWidget from '@/components/ChatWidget.vue'

const activities = ref<Activity[]>([])
const searchQuery = ref('')
const loading = ref(true)

async function loadActivities() {
  loading.value = true
  try {
    activities.value = await fetchActivities()
  } catch (error) {
    console.error('Failed to load activities:', error)
  } finally {
    loading.value = false
  }
}

async function handleSearch() {
  loading.value = true
  try {
    if (searchQuery.value.trim()) {
      activities.value = await searchActivities({ query: searchQuery.value })
    } else {
      activities.value = await fetchActivities()
    }
  } catch (error) {
    console.error('Search failed:', error)
  } finally {
    loading.value = false
  }
}
function handleRecommendations(activityIds: number[]) {
  // Sort recommended activities to the top
  const recommended = activities.value.filter((a) => activityIds.includes(a.id))
  const others = activities.value.filter((a) => !activityIds.includes(a.id))
  activities.value = [...recommended, ...others]
}

onMounted(loadActivities)
</script>

<template>
  <main class="home">
    <section class="hero">
      <h1>AI Travel Concierge</h1>
      <p>Describe your perfect trip, and let AI find the best experiences for you.</p>
      <div class="search-bar">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Search activities... (e.g. food, museum, outdoor)"
          @keyup.enter="handleSearch"
        />
        <button @click="handleSearch">Search</button>
      </div>
    </section>

    <section class="activities">
      <div v-if="loading" class="loading">Loading experiences...</div>
      <div v-else-if="activities.length === 0" class="empty">
        No activities found. Try a different search.
      </div>
      <div v-else class="activity-grid">
        <ActivityCard v-for="activity in activities" :key="activity.id" :activity="activity" />
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

.hero {
  text-align: center;
  padding: 3rem 0 2rem;
}

.hero h1 {
  font-size: 2.5rem;
  font-weight: 700;
  color: #ff5533;
  margin-bottom: 0.75rem;
}

.hero p {
  font-size: 1.15rem;
  color: #666;
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

.search-bar input:focus {
  border-color: #ff5533;
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

.search-bar button:hover {
  background: #e64a2e;
}

.activities {
  padding: 2rem 0 4rem;
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.loading,
.empty {
  text-align: center;
  padding: 3rem;
  color: #999;
  font-size: 1.1rem;
}
</style>
