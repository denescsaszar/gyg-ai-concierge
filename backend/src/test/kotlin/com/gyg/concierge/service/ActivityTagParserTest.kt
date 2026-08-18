package com.gyg.concierge.service

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ActivityTagParserTest {

    private val known = setOf(1L, 4L, 7L)

    @Test
    fun `preserves the order Claude ranked the ids in`() {
        assertEquals(listOf(7L, 4L, 1L), ActivityTagParser.parseIds("Great picks [ACTIVITIES:7,4,1]", known))
    }

    @Test
    fun `drops ids that are not in the catalog`() {
        assertEquals(listOf(4L), ActivityTagParser.parseIds("[ACTIVITIES:4,42,99]", known))
    }

    @Test
    fun `de-duplicates repeated ids`() {
        assertEquals(listOf(4L, 7L), ActivityTagParser.parseIds("[ACTIVITIES:4,7,4]", known))
    }

    @Test
    fun `returns nothing when there is no tag`() {
        assertEquals(emptyList(), ActivityTagParser.parseIds("Could you tell me your budget?", known))
    }

    @Test
    fun `strips the tag and surrounding whitespace from the reply`() {
        assertEquals(
            "I'd start with the kayak tour.",
            ActivityTagParser.stripTag("I'd start with the kayak tour. [ACTIVITIES:4,1]"),
        )
    }

    @Test
    fun `leaves a reply without a tag untouched`() {
        assertEquals("What is your budget?", ActivityTagParser.stripTag("What is your budget?"))
    }
}
