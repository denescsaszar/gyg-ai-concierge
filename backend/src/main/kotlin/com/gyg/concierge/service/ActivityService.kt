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
        return SampleActivities.activities.filter { activity ->
            val matchesQuery = query?.let {
                activity.title.contains(it, ignoreCase = true) ||
                activity.description.contains(it, ignoreCase = true)
            } ?: true

            val matchesCategory = category?.let { activity.category == it } ?: true
            val matchesPrice = maxPrice?.let { activity.priceEur <= it } ?: true
            val matchesDuration = maxDuration?.let { activity.durationMinutes <= it } ?: true

            matchesQuery && matchesCategory && matchesPrice && matchesDuration
        }
    }
}
