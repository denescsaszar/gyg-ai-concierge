package com.gyg.concierge.service

/**
 * Claude signals which cards to highlight by embedding `[ACTIVITIES:4,1,7]` in
 * its reply. Extracted from ConciergeService so the parsing rules can be tested
 * without an API key or a network call.
 */
object ActivityTagParser {

    private val TAG = Regex("\\[ACTIVITIES:([\\d,]+)]")

    /** Ids in the order Claude ranked them, with anything not in [knownIds] dropped. */
    fun parseIds(text: String, knownIds: Set<Long>): List<Long> {
        val match = TAG.find(text) ?: return emptyList()
        return match.groupValues[1]
            .split(",")
            .mapNotNull { it.trim().toLongOrNull() }
            .filter { it in knownIds }
            .distinct()
    }

    /** The reply with the machine-readable tag stripped out. */
    fun stripTag(text: String): String = text.replace(TAG, "").trim()
}
