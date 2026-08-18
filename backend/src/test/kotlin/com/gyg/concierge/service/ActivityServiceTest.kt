package com.gyg.concierge.service

import com.gyg.concierge.model.Category
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ActivityServiceTest {

    private val service = ActivityService()

    @Test
    fun `returns the whole catalog`() {
        assertEquals(10, service.getAllActivities().size)
    }

    @Test
    fun `finds an activity by id`() {
        val activity = service.getActivityById(4)

        assertNotNull(activity)
        assertEquals("Kayak Tour on the Spree River", activity.title)
    }

    @Test
    fun `returns null for an unknown id`() {
        assertNull(service.getActivityById(999))
    }

    @Test
    fun `search matches the title`() {
        val results = service.searchActivities(query = "kayak")

        assertEquals(1, results.size)
        assertEquals(4L, results.first().id)
    }

    @Test
    fun `search matches highlights, not just title and description`() {
        val results = service.searchActivities(query = "vegetarian")

        assertTrue(results.isNotEmpty(), "highlights should be searchable")
        assertTrue(results.all { it.highlights.any { h -> h.contains("egetarian") } })
    }

    @Test
    fun `search matches the category name`() {
        val results = service.searchActivities(query = "food and drink")

        assertTrue(results.any { it.category == Category.FOOD_AND_DRINK })
    }

    @Test
    fun `search is case insensitive and ignores surrounding whitespace`() {
        assertEquals(
            service.searchActivities(query = "kayak").map { it.id },
            service.searchActivities(query = "  KAYAK  ").map { it.id },
        )
    }

    @Test
    fun `a blank query is treated as no query`() {
        assertEquals(10, service.searchActivities(query = "   ").size)
    }

    @Test
    fun `filters by max price inclusively`() {
        val results = service.searchActivities(maxPrice = 25.0)

        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.priceEur <= 25.0 })
    }

    @Test
    fun `filters by max duration`() {
        val results = service.searchActivities(maxDuration = 90)

        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.durationMinutes <= 90 })
    }

    @Test
    fun `filters combine`() {
        val results = service.searchActivities(category = Category.TOURS, maxPrice = 20.0)

        assertTrue(results.all { it.category == Category.TOURS && it.priceEur <= 20.0 })
    }

    @Test
    fun `unmatched query returns empty rather than everything`() {
        assertTrue(service.searchActivities(query = "scuba diving in the alps").isEmpty())
    }
}
