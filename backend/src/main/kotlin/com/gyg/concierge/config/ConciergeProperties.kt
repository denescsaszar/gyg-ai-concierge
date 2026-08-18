package com.gyg.concierge.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Guardrails for the one endpoint that costs real money per call.
 *
 * `/api/concierge/chat` is public and unauthenticated by design (it's a demo),
 * which means anyone who finds the URL can spend the project's Anthropic budget.
 * These caps bound both the size of a single call and how many a client can make.
 */
@ConfigurationProperties(prefix = "concierge")
data class ConciergeProperties(
    /** Longest single user message we will forward to Claude. */
    val maxMessageLength: Int = 1000,
    /** How many prior turns we keep; older ones are dropped before the call. */
    val maxHistoryMessages: Int = 12,
    val rateLimit: RateLimit = RateLimit(),
) {
    data class RateLimit(
        val requestsPerWindow: Int = 10,
        val windowSeconds: Long = 60,
    )
}
