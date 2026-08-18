package com.gyg.concierge.service

import com.anthropic.client.AnthropicClient
import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.errors.AnthropicException
import com.anthropic.models.messages.MessageCreateParams
import com.anthropic.models.messages.Model
import com.anthropic.models.messages.MessageParam
import com.gyg.concierge.config.ConciergeProperties
import com.gyg.concierge.data.SampleActivities
import com.gyg.concierge.model.ChatRequest
import com.gyg.concierge.model.ChatResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ConciergeService(
    @Value("\${anthropic.api.key}") private val apiKey: String,
    private val properties: ConciergeProperties,
) {

    private val log = LoggerFactory.getLogger(ConciergeService::class.java)

    private val client: AnthropicClient by lazy {
        AnthropicOkHttpClient.builder()
            .apiKey(apiKey)
            .build()
    }

    /**
     * The catalog is static, so the prompt is too. It used to be rebuilt from
     * scratch on every request.
     */
    private val systemPrompt: String by lazy { buildSystemPrompt() }

    private fun buildSystemPrompt(): String {
        val activitiesJson = SampleActivities.activities.joinToString("\n") { activity ->
            """- ID:${activity.id} | ${activity.title} | ${activity.category} | €${activity.priceEur} | ${activity.durationMinutes}min | Rating:${activity.rating} | ${activity.description}"""
        }

        return """You are an AI travel concierge for GetYourGuide, helping travelers discover amazing experiences in Berlin.

You have access to these available activities — this catalog is your ONLY source of recommendations:
$activitiesJson

Your job:
1. Understand what the traveler is looking for (interests, budget, available time, group size, and anything already discussed)
2. Select and rank the most relevant activities from the catalog above — strongest match first
3. Explain WHY each recommendation fits their needs, in the same order as your ranking
4. Be warm, enthusiastic, and knowledgeable — like a local friend

Catalog rules (strict):
- ONLY recommend activities listed in the catalog above — never invent, rename, or suggest experiences outside this list
- Every recommended activity MUST use its exact catalog ID; verify the ID exists in the catalog before mentioning it
- Recommend 1–5 activities per response unless the traveler asks for more
- Rank recommendations by relevance: lead with the best match, then strong alternatives
- Order the [ACTIVITIES:...] tag from most to least relevant (e.g. [ACTIVITIES:4,1,7])

Conversation rules:
- Do NOT repeat activities you already recommended in this conversation unless the traveler explicitly asks to revisit or compare them
- When the traveler refines their criteria, prioritize NEW catalog options that fit better; mention prior suggestions only if they remain top-ranked
- If nothing matches well, say so honestly and suggest the closest catalog alternatives with a brief explanation

Follow-up questions:
- Ask follow-up questions ONLY when the request is too vague to rank activities confidently
- Make questions specific and actionable — reference concrete trade-offs (e.g. "Under €30 or happy to splurge?", "Half-day or full-day?", "History and culture, or food and nightlife?", "Traveling with kids?")
- Ask at most 1–2 focused questions, never a generic open-ended list

Output format:
- Keep responses concise but helpful (2–3 paragraphs max)
- When recommending activities, always end with IDs in this exact format: [ACTIVITIES:1,4,7] (most relevant first)
- If you need more information before recommending, ask your follow-up question(s) and omit the [ACTIVITIES:...] tag until you can make ranked recommendations"""
    }

    fun chat(request: ChatRequest): ChatResponse {
        val messages = mutableListOf<MessageParam>()

        // Trim to the most recent turns. Bean validation already rejects absurd
        // payloads; this keeps the token cost of a legitimate long chat bounded.
        val history = request.conversationHistory.takeLast(properties.maxHistoryMessages)

        for (msg in history) {
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
                .content(request.message.take(properties.maxMessageLength))
                .build()
        )

        val params = MessageCreateParams.builder()
            .model(ACTIVE_AI_MODEL)
            .maxTokens(1024)
            .system(MessageCreateParams.System.ofString(systemPrompt))
            .messages(messages)
            .build()

        return try {
            val response = client.messages().create(params)

            val responseText = response.content()
                .filter { it.isText() }
                .joinToString("") { it.asText().text() }

            // Hallucinated ids are dropped here so the frontend is never asked to
            // highlight a card that does not exist.
            val activityIds = ActivityTagParser.parseIds(responseText, knownActivityIds)
            val cleanedMessage = ActivityTagParser.stripTag(responseText)

            ChatResponse(
                message = cleanedMessage,
                recommendedActivityIds = activityIds
            )
        } catch (e: AnthropicException) {
            log.error("Anthropic API call failed", e)
            ChatResponse(message = AI_UNAVAILABLE_MESSAGE)
        }
    }

    private val knownActivityIds: Set<Long> by lazy {
        SampleActivities.activities.mapTo(mutableSetOf()) { it.id }
    }

    companion object {
        val ACTIVE_AI_MODEL: Model = Model.CLAUDE_SONNET_4_6

        private const val AI_UNAVAILABLE_MESSAGE =
            "Sorry, I'm having trouble reaching the AI service right now. Please try again in a moment, or browse the available experiences below."
    }
}
