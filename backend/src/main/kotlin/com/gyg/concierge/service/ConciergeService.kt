package com.gyg.concierge.service

import com.anthropic.client.AnthropicClient
import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.errors.AnthropicException
import com.anthropic.models.messages.MessageCreateParams
import com.anthropic.models.messages.Model
import com.anthropic.models.messages.MessageParam
import com.gyg.concierge.data.SampleActivities
import com.gyg.concierge.model.ChatRequest
import com.gyg.concierge.model.ChatResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ConciergeService(
    @Value("\${anthropic.api.key}") private val apiKey: String
) {

    private val log = LoggerFactory.getLogger(ConciergeService::class.java)

    private val client: AnthropicClient by lazy {
        AnthropicOkHttpClient.builder()
            .apiKey(apiKey)
            .build()
    }

    private fun buildSystemPrompt(): String {
        val activitiesJson = SampleActivities.activities.joinToString("\n") { activity ->
            """- ID:${activity.id} | ${activity.title} | ${activity.category} | €${activity.priceEur} | ${activity.durationMinutes}min | Rating:${activity.rating} | ${activity.description}"""
        }

        return """You are an AI travel concierge for GetYourGuide, helping travelers discover amazing experiences in Berlin.

You have access to these available activities:
$activitiesJson

Your job:
1. Understand what the traveler is looking for (interests, budget, time, group type)
2. Recommend the most relevant activities from the catalog above
3. Explain WHY each recommendation fits their needs
4. Be warm, enthusiastic, and knowledgeable — like a local friend

Rules:
- Only recommend activities from the catalog above
- Always include the activity IDs in your response in this exact format: [ACTIVITIES:1,4,7] (put this at the very end of your message)
- If nothing matches well, say so honestly and suggest what's closest
- Keep responses concise but helpful (2-3 paragraphs max)
- Ask follow-up questions if the request is vague"""
    }

    fun chat(request: ChatRequest): ChatResponse {
        val messages = mutableListOf<MessageParam>()

        for (msg in request.conversationHistory) {
            messages.add(
                MessageParam.builder()
                    .role(if (msg.role == "user") MessageParam.Role.USER else MessageParam.Role.ASSISTANT)
                    .content(msg.content)
                    .build()
            )
        }

        messages.add(
            MessageParam.builder()
                .role(MessageParam.Role.USER)
                .content(request.message)
                .build()
        )

        val params = MessageCreateParams.builder()
            .model(Model.CLAUDE_SONNET_4_6)
            .maxTokens(1024)
            .system(MessageCreateParams.System.ofString(buildSystemPrompt()))
            .messages(messages)
            .build()

        return try {
            val response = client.messages().create(params)

            val responseText = response.content()
                .filter { it.isText() }
                .joinToString("") { it.asText().text() }

            val activityIds = parseActivityIds(responseText)
            val cleanedMessage = responseText.replace(Regex("\\[ACTIVITIES:[\\d,]+]"), "").trim()

            ChatResponse(
                message = cleanedMessage,
                recommendedActivityIds = activityIds
            )
        } catch (e: AnthropicException) {
            log.error("Anthropic API call failed", e)
            ChatResponse(message = AI_UNAVAILABLE_MESSAGE)
        }
    }

    companion object {
        private const val AI_UNAVAILABLE_MESSAGE =
            "Sorry, I'm having trouble reaching the AI service right now. Please try again in a moment, or browse the available experiences below."
    }

    private fun parseActivityIds(text: String): List<Long> {
        val regex = Regex("\\[ACTIVITIES:([\\d,]+)]")
        val match = regex.find(text) ?: return emptyList()
        return match.groupValues[1].split(",").mapNotNull { it.trim().toLongOrNull() }
    }
}
