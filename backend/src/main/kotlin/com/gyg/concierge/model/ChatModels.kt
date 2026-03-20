package com.gyg.concierge.model

data class ChatRequest(
    val message: String,
    val conversationHistory: List<ChatMessage> = emptyList()
)

data class ChatMessage(
    val role: String,
    val content: String
)

data class ChatResponse(
    val message: String,
    val recommendedActivityIds: List<Long> = emptyList()
)
