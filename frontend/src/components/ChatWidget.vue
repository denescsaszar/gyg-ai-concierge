<script setup lang="ts">
import { ref, nextTick } from 'vue'
import type { ChatMessage, ChatResponse } from '@/types/activity'
import { sendChatMessage } from '@/services/api'

const emit = defineEmits<{
  recommendations: [activityIds: number[]]
}>()

const messages = ref<ChatMessage[]>([])
const userInput = ref('')
const loading = ref(false)
const chatContainer = ref<HTMLElement | null>(null)
const isOpen = ref(false)

async function handleSend() {
  const text = userInput.value.trim()
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: text })
  userInput.value = ''
  loading.value = true

  await scrollToBottom()

  try {
    const history = messages.value.slice(0, -1)
    const response: ChatResponse = await sendChatMessage(text, history)

    messages.value.push({ role: 'assistant', content: response.message })

    if (response.recommendedActivityIds.length > 0) {
      emit('recommendations', response.recommendedActivityIds)
    }
  } catch (error) {
    messages.value.push({
      role: 'assistant',
      content: 'Sorry, I had trouble processing that. Please try again.',
    })
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

async function scrollToBottom() {
  await nextTick()
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}
</script>

<template>
  <div class="chat-widget">
    <button class="chat-toggle" @click="isOpen = !isOpen">
      {{ isOpen ? '✕' : '💬 Ask AI Concierge' }}
    </button>

    <div v-if="isOpen" class="chat-panel">
      <div class="chat-header">
        <h3>AI Travel Concierge</h3>
        <p>Ask me anything about Berlin experiences!</p>
      </div>

      <div ref="chatContainer" class="chat-messages">
        <div v-if="messages.length === 0" class="chat-empty">
          <p>Hi! I'm your AI travel concierge. Tell me what you're looking for:</p>
          <div class="suggestions">
            <button @click="userInput = 'I have 2 days in Berlin with kids'; handleSend()">
              2 days with kids
            </button>
            <button @click="userInput = 'Best food experiences in Berlin'; handleSend()">
              Food experiences
            </button>
            <button @click="userInput = 'Outdoor activities under €40'; handleSend()">
              Outdoor under €40
            </button>
          </div>
        </div>

        <div v-for="(msg, index) in messages" :key="index" class="message" :class="msg.role">
          <div class="message-bubble">{{ msg.content }}</div>
        </div>

        <div v-if="loading" class="message assistant">
          <div class="message-bubble typing"><span></span><span></span><span></span></div>
        </div>
      </div>

      <div class="chat-input">
        <input
          v-model="userInput"
          type="text"
          placeholder="Describe your ideal experience..."
          @keyup.enter="handleSend"
          :disabled="loading"
        />
        <button @click="handleSend" :disabled="loading || !userInput.trim()">Send</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
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

.chat-panel {
  position: absolute;
  bottom: 60px;
  right: 0;
  width: 400px;
  height: 520px;
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
  opacity: 0.9;
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
  color: #666;
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
  background: #ff5533;
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
  background: #999;
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
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 0.9rem;
  outline: none;
}

.chat-input input:focus {
  border-color: #ff5533;
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
</style>
