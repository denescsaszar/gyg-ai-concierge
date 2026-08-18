import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import HomeView from '../views/HomeView.vue'
import { FALLBACK_ACTIVITIES } from '../data/activities'
import * as api from '../services/api'

vi.mock('../services/api')

describe('HomeView', () => {
  beforeEach(() => {
    vi.mocked(api.fetchActivities).mockReset()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('renders the full catalog before the API responds — no blocking spinner', async () => {
    // A request that never settles stands in for a cold-starting backend.
    vi.mocked(api.fetchActivities).mockReturnValue(new Promise(() => {}))

    const wrapper = mount(HomeView)

    expect(wrapper.findAllComponents({ name: 'ActivityCard' })).toHaveLength(
      FALLBACK_ACTIVITIES.length,
    )
    expect(wrapper.text()).toContain('syncing with server')
  })

  it('swaps in live data once the API answers', async () => {
    const live = [{ ...FALLBACK_ACTIVITIES[0]!, title: 'Live Tour From The API' }]
    vi.mocked(api.fetchActivities).mockResolvedValue(live)

    const wrapper = mount(HomeView)
    await flushPromises()

    expect(wrapper.findAllComponents({ name: 'ActivityCard' })).toHaveLength(1)
    expect(wrapper.text()).toContain('Live Tour From The API')
    expect(wrapper.text()).not.toContain('syncing with server')
  })

  it('keeps showing the snapshot when the backend is unreachable', async () => {
    vi.mocked(api.fetchActivities).mockRejectedValue(new Error('offline'))
    vi.spyOn(console, 'warn').mockImplementation(() => {})

    const wrapper = mount(HomeView)
    await flushPromises()

    expect(wrapper.findAllComponents({ name: 'ActivityCard' })).toHaveLength(
      FALLBACK_ACTIVITIES.length,
    )
    expect(wrapper.text()).toContain('showing the built-in catalog')
  })

  it('filters locally across title, description and highlights', async () => {
    vi.mocked(api.fetchActivities).mockResolvedValue(FALLBACK_ACTIVITIES)

    const wrapper = mount(HomeView)
    await flushPromises()

    await wrapper.find('input[type="search"]').setValue('kayak')

    const cards = wrapper.findAllComponents({ name: 'ActivityCard' })
    expect(cards).toHaveLength(1)
    expect(cards[0]!.text()).toContain('Kayak Tour on the Spree River')
  })

  it('shows an empty state for a query that matches nothing', async () => {
    vi.mocked(api.fetchActivities).mockResolvedValue(FALLBACK_ACTIVITIES)

    const wrapper = mount(HomeView)
    await flushPromises()

    await wrapper.find('input[type="search"]').setValue('scuba diving in the alps')

    expect(wrapper.findAllComponents({ name: 'ActivityCard' })).toHaveLength(0)
    expect(wrapper.text()).toContain('No activities match')
  })

  it('sorts concierge recommendations to the top in the order they were returned', async () => {
    vi.mocked(api.fetchActivities).mockResolvedValue(FALLBACK_ACTIVITIES)

    const wrapper = mount(HomeView)
    await flushPromises()

    wrapper.findComponent({ name: 'ChatWidget' }).vm.$emit('recommendations', [7, 4])
    await flushPromises()

    const titles = wrapper.findAllComponents({ name: 'ActivityCard' }).map((c) => c.props('activity').id)
    expect(titles.slice(0, 2)).toEqual([7, 4])
  })
})
