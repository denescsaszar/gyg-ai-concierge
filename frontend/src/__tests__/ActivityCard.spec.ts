import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ActivityCard from '../components/ActivityCard.vue'
import { FALLBACK_ACTIVITIES } from '../data/activities'

const activity = FALLBACK_ACTIVITIES[0]!
const freeActivity = FALLBACK_ACTIVITIES.find((a) => a.priceEur === 0)!

describe('ActivityCard', () => {
  it('requests a resized, auto-formatted image instead of the full-size original', () => {
    const img = mount(ActivityCard, { props: { activity } }).get('img')

    expect(img.attributes('src')).toContain('auto=format')
    expect(img.attributes('src')).toContain('w=400')
    expect(img.attributes('srcset')).toContain('w=800')
  })

  it('reserves layout space so images do not shift the grid', () => {
    const img = mount(ActivityCard, { props: { activity } }).get('img')

    expect(img.attributes('width')).toBe('400')
    expect(img.attributes('height')).toBe('250')
  })

  it('defers off-screen images and prioritises the first row', () => {
    expect(mount(ActivityCard, { props: { activity } }).get('img').attributes('loading')).toBe('lazy')
    expect(
      mount(ActivityCard, { props: { activity, priority: true } }).get('img').attributes('loading'),
    ).toBe('eager')
  })

  it('gives every image a descriptive alt text', () => {
    const img = mount(ActivityCard, { props: { activity } }).get('img')

    expect(img.attributes('alt')).toBe(`${activity.title} in ${activity.city}`)
  })

  it('formats duration and price for humans', () => {
    const text = mount(ActivityCard, { props: { activity } }).text()

    expect(text).toContain('2h 30min')
    expect(text).toContain('€18')
  })

  it('labels a zero price as Free rather than €0', () => {
    expect(mount(ActivityCard, { props: { activity: freeActivity } }).text()).toContain('Free')
  })

  it('only shows the AI badge when recommended', () => {
    expect(mount(ActivityCard, { props: { activity } }).text()).not.toContain('AI Recommended')
    expect(mount(ActivityCard, { props: { activity, recommended: true } }).text()).toContain(
      'AI Recommended',
    )
  })
})
