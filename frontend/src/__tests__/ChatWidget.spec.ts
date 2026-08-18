import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import ChatWidget from '../components/ChatWidget.vue'
import * as api from '../services/api'

vi.mock('../services/api')

async function openWidget() {
  const wrapper = mount(ChatWidget, { attachTo: document.body })
  await wrapper.get('.chat-toggle').trigger('click')
  return wrapper
}

describe('ChatWidget', () => {
  beforeEach(() => {
    vi.mocked(api.sendChatMessage).mockReset()
  })

  it('renders markdown bold as real emphasis instead of literal asterisks', async () => {
    vi.mocked(api.sendChatMessage).mockResolvedValue({
      message: 'Try the **Kayak Tour** — it is lovely.',
      recommendedActivityIds: [],
    })

    const wrapper = await openWidget()
    await wrapper.get('#concierge-input').setValue('something romantic')
    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(wrapper.get('.assistant .message-bubble strong').text()).toBe('Kayak Tour')
    expect(wrapper.text()).not.toContain('**')
  })

  it('emits the recommended ids so the grid can re-rank', async () => {
    vi.mocked(api.sendChatMessage).mockResolvedValue({
      message: 'Here you go.',
      recommendedActivityIds: [4, 7],
    })

    const wrapper = await openWidget()
    await wrapper.get('#concierge-input').setValue('outdoor please')
    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(wrapper.emitted('recommendations')?.[0]).toEqual([[4, 7]])
  })

  it('only sends the most recent turns as history', async () => {
    vi.mocked(api.sendChatMessage).mockResolvedValue({ message: 'ok', recommendedActivityIds: [] })

    const wrapper = await openWidget()
    for (let i = 0; i < 8; i++) {
      await wrapper.get('#concierge-input').setValue(`message ${i}`)
      await wrapper.get('form').trigger('submit')
      await flushPromises()
    }

    const calls = vi.mocked(api.sendChatMessage).mock.calls
    const lastCall = calls[calls.length - 1]!
    expect(lastCall[1].length).toBeLessThanOrEqual(12)
  })

  it('caps the input length rather than letting a huge prompt reach the API', async () => {
    const wrapper = await openWidget()

    expect(wrapper.get('#concierge-input').attributes('maxlength')).toBe('1000')
  })

  it('surfaces a recoverable message when the request fails', async () => {
    vi.mocked(api.sendChatMessage).mockRejectedValue(new Error('boom'))
    vi.spyOn(console, 'error').mockImplementation(() => {})

    const wrapper = await openWidget()
    await wrapper.get('#concierge-input').setValue('hello')
    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(wrapper.text()).toContain("couldn't reach the concierge")
  })

  it('exposes open state to assistive tech', async () => {
    const wrapper = mount(ChatWidget)

    expect(wrapper.get('.chat-toggle').attributes('aria-expanded')).toBe('false')
    await wrapper.get('.chat-toggle').trigger('click')
    expect(wrapper.get('.chat-toggle').attributes('aria-expanded')).toBe('true')
    expect(wrapper.get('[role="dialog"]').attributes('aria-label')).toBe('AI Travel Concierge')
  })
})
