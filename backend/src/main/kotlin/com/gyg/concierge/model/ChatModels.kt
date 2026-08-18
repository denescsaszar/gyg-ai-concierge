package com.gyg.concierge.model

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class ChatRequest(
    @field:NotBlank(message = "message must not be blank")
    @field:Size(max = 1000, message = "message must be 1000 characters or fewer")
    val message: String,

    // Unbounded history used to be forwarded straight to Claude, so a single
    // crafted request could run up an arbitrarily large token bill.
    @field:Valid
    @field:Size(max = 40, message = "conversationHistory is too long")
    val conversationHistory: List<ChatMessage> = emptyList(),
)

data class ChatMessage(
    @field:Pattern(regexp = "user|assistant", message = "role must be 'user' or 'assistant'")
    val role: String,

    @field:Size(max = 4000, message = "history entries must be 4000 characters or fewer")
    val content: String,
)

data class ChatResponse(
    val message: String,
    val recommendedActivityIds: List<Long> = emptyList(),
)
