<script setup lang="ts">
import { nextTick, onUnmounted, ref, watch } from 'vue'
import type { ChatMessage, ChatResponse } from '@/types/activity'
import { sendChatMessage } from '@/services/api'

const emit = defineEmits<{
  recommendations: [activityIds: number[]]
}>()

/** Matches the backend's own caps so we fail fast in the UI instead of on the wire. */
const MAX_MESSAGE_LENGTH = 1000
/** Only the recent turns are worth the tokens; older ones are dropped. */
const MAX_HISTORY_TURNS = 12
/** How long before we admit the backend is cold-starting. */
const COLD_START_HINT_MS = 4000

const messages = ref<ChatMessage[]>([])
const userInput = ref('')
const loading = ref(false)
const showColdStartHint = ref(false)
const chatContainer = ref<HTMLElement | null>(null)
const inputEl = ref<HTMLInputElement | null>(null)
const isOpen = ref(false)

let controller: AbortController | null = null
let hintTimer: ReturnType<typeof setTimeout> | null = null

/**
 * Claude replies in markdown. Rendering the raw string showed literal `**` to
 * users; rendering it with v-html would be an XSS hole. This splits into plain
 * and bold segments that Vue escapes normally.
 */
function segments(text: string): Array<{ text: string; bold: boolean }> {
  return text
    .split(/(\*\*[^*]+\*\*)/g)
    .filter(Boolean)
    .map((part) =>
      part.startsWith('**') && part.endsWith('**')
        ? { text: part.slice(2, -2), bold: true }
        : { text: part, bold: false },
    )
}

async function handleSend(preset?: string) {
  const text = (preset ?? userInput.value).trim().slice(0, MAX_MESSAGE_LENGTH)
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: text })
  userInput.value = ''
  loading.value = true
  controller = new AbortController()
  hintTimer = setTimeout(() => (showColdStartHint.value = true), COLD_START_HINT_MS)

  await scrollToBottom()

  try {
    const history = messages.value.slice(0, -1).slice(-MAX_HISTORY_TURNS)
    const response: ChatResponse = await sendChatMessage(text, history, controller.signal)

    messages.value.push({ role: 'assistant', content: response.message })

    if (response.recommendedActivityIds.length > 0) {
      emit('recommendations', response.recommendedActivityIds)
    }
  } catch (error) {
    if (!controller.signal.aborted) {
      console.error('Chat request failed:', error)
      messages.value.push({
        role: 'assistant',
        content:
          "Sorry, I couldn't reach the concierge. The server may still be waking up — give it a moment and try again.",
      })
    }
  } finally {
    if (hintTimer) clearTimeout(hintTimer)
    showColdStartHint.value = false
    loading.value = false
    controller = null
    await scrollToBottom()
  }
}

async function scrollToBottom() {
  await nextTick()
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}

function close() {
  isOpen.value = false
}

watch(isOpen, async (open) => {
  if (!open) return
  await nextTick()
  inputEl.value?.focus()
})

onUnmounted(() => {
  controller?.abort()
  if (hintTimer) clearTimeout(hintTimer)
})
</script>

<template>
  <div class="chat-widget">
    <button
      class="chat-toggle"
      type="button"
      aria-controls="concierge-panel"
      :aria-expanded="isOpen"
      :aria-label="isOpen ? 'Close the AI concierge' : 'Open the AI concierge'"
      @click="isOpen = !isOpen"
    >
      <span aria-hidden="true">{{ isOpen ? '✕' : '💬 Ask AI Concierge' }}</span>
    </button>

    <div
      v-if="isOpen"
      id="concierge-panel"
      class="chat-panel"
      role="dialog"
      aria-label="AI Travel Concierge"
      @keydown.esc="close"
    >
      <div class="chat-header">
        <h3>AI Travel Concierge</h3>
        <p>Ask me anything about Berlin experiences!</p>
      </div>

      <div ref="chatContainer" class="chat-messages" aria-live="polite" aria-atomic="false">
        <div v-if="messages.length === 0" class="chat-empty">
          <p>Hi! I'm your AI travel concierge. Tell me what you're looking for:</p>
          <div class="suggestions">
            <button type="button" @click="handleSend('I have 2 days in Berlin with kids')">
              2 days with kids
            </button>
            <button type="button" @click="handleSend('Best food experiences in Berlin')">
              Food experiences
            </button>
            <button type="button" @click="handleSend('Outdoor activities under €40')">
              Outdoor under €40
            </button>
          </div>
        </div>

        <div v-for="(msg, index) in messages" :key="index" class="message" :class="msg.role">
          <div class="message-bubble">
            <template v-for="(seg, i) in segments(msg.content)" :key="i">
              <strong v-if="seg.bold">{{ seg.text }}</strong>
              <template v-else>{{ seg.text }}</template>
            </template>
          </div>
        </div>

        <div v-if="loading" class="message assistant">
          <div class="message-bubble typing" role="status" aria-label="Concierge is typing">
            <span></span><span></span><span></span>
          </div>
        </div>
        <p v-if="showColdStartHint" class="cold-start-hint">
          Waking the server up — free hosting, this can take up to a minute.
        </p>
      </div>

      <form class="chat-input" @submit.prevent="handleSend()">
        <label class="sr-only" for="concierge-input">Message the concierge</label>
        <input
          id="concierge-input"
          ref="inputEl"
          v-model="userInput"
          type="text"
          :maxlength="MAX_MESSAGE_LENGTH"
          placeholder="Describe your ideal experience..."
          :disabled="loading"
        />
        <button type="submit" :disabled="loading || !userInput.trim()">Send</button>
      </form>
    </div>
  </div>
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

.chat-widget {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 1000;
}

.chat-toggle {
  background: #ff5533;
  color: white;
  border: none;
  padding: 14px 24px;
  border-radius: 50px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(255, 85, 51, 0.3);
  transition:
    transform 0.2s,
    box-shadow 0.2s;
}

.chat-toggle:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 85, 51, 0.4);
}

.chat-toggle:focus-visible,
.chat-input button:focus-visible,
.suggestions button:focus-visible {
  outline: 2px solid #1a1a1a;
  outline-offset: 2px;
}

.chat-panel {
  position: absolute;
  bottom: 60px;
  right: 0;
  width: min(400px, calc(100vw - 32px));
  height: min(520px, calc(100vh - 120px));
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  background: #ff5533;
  color: white;
  padding: 16px 20px;
}

.chat-header h3 {
  font-size: 1.1rem;
  margin-bottom: 4px;
}

.chat-header p {
  font-size: 0.8rem;
  opacity: 0.95;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chat-empty {
  text-align: center;
  color: #595959;
  padding: 20px 0;
}

.chat-empty p {
  margin-bottom: 16px;
  font-size: 0.95rem;
}

.suggestions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.suggestions button {
  background: #f5f5f5;
  border: 1px solid #e0e0e0;
  padding: 10px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.85rem;
  transition: background 0.2s;
}

.suggestions button:hover {
  background: #ffe8e3;
  border-color: #ff5533;
}

.cold-start-hint {
  font-size: 0.78rem;
  color: #595959;
  text-align: center;
  padding: 0 8px;
}

.message {
  display: flex;
}

.message.user {
  justify-content: flex-end;
}

.message-bubble {
  max-width: 85%;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 0.9rem;
  line-height: 1.5;
  white-space: pre-wrap;
}

.user .message-bubble {
  background: #d13b1c;
  color: white;
  border-bottom-right-radius: 4px;
}

.assistant .message-bubble {
  background: #f0f0f0;
  color: #1a1a1a;
  border-bottom-left-radius: 4px;
}

.typing {
  display: flex;
  gap: 4px;
  padding: 14px 18px;
}

.typing span {
  width: 8px;
  height: 8px;
  background: #767676;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out;
}

.typing span:nth-child(2) {
  animation-delay: 0.2s;
}
.typing span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes bounce {
  0%,
  80%,
  100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

.chat-input {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
}

.chat-input input {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid #c4c4c4;
  border-radius: 8px;
  font-size: 0.9rem;
  outline: none;
}

.chat-input input:focus-visible {
  border-color: #ff5533;
  outline: 2px solid #d13b1c;
  outline-offset: 1px;
}

.chat-input button {
  background: #ff5533;
  color: white;
  border: none;
  padding: 10px 18px;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  font-size: 0.9rem;
}

.chat-input button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (prefers-reduced-motion: reduce) {
  .chat-toggle,
  .chat-toggle:hover {
    transition: none;
    transform: none;
  }

  .typing span {
    animation: none;
    transform: scale(1);
  }
}
</style>
