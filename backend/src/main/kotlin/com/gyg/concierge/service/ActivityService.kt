package com.gyg.concierge.service

import com.gyg.concierge.data.SampleActivities
import com.gyg.concierge.model.Activity
import com.gyg.concierge.model.Category
import org.springframework.stereotype.Service

@Service
class ActivityService {

    fun getAllActivities(): List<Activity> = SampleActivities.activities

    fun getActivityById(id: Long): Activity? =
        SampleActivities.activities.find { it.id == id }

    fun searchActivities(
        query: String? = null,
        category: Category? = null,
        maxPrice: Double? = null,
        maxDuration: Int? = null
    ): List<Activity> {
        val normalisedQuery = query?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }

        return SampleActivities.activities.filter { activity ->
            // Previously only title and description were searched, so "vegetarian
            // options" or "Berlin" returned nothing despite being in the data —
            // and the README claimed highlights were covered.
            val matchesQuery = normalisedQuery?.let { activity.searchableText().contains(it) } ?: true
            val matchesCategory = category?.let { activity.category == it } ?: true
            val matchesPrice = maxPrice?.let { activity.priceEur <= it } ?: true
            val matchesDuration = maxDuration?.let { activity.durationMinutes <= it } ?: true

            matchesQuery && matchesCategory && matchesPrice && matchesDuration
        }
    }

    private fun Activity.searchableText(): String =
        buildString {
            append(title).append(' ')
            append(description).append(' ')
            append(city).append(' ')
            append(category.name.replace('_', ' ')).append(' ')
            highlights.forEach { append(it).append(' ') }
        }.lowercase()
}
