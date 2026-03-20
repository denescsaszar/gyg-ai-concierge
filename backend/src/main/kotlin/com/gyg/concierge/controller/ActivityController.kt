package com.gyg.concierge.controller

import com.gyg.concierge.model.Activity
import com.gyg.concierge.model.Category
import com.gyg.concierge.service.ActivityService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/activities")
class ActivityController(private val activityService: ActivityService) {

    @GetMapping
    fun getAllActivities(): List<Activity> = activityService.getAllActivities()

    @GetMapping("/{id}")
    fun getActivityById(@PathVariable id: Long): ResponseEntity<Activity> {
        val activity = activityService.getActivityById(id)
        return if (activity != null) {
            ResponseEntity.ok(activity)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/search")
    fun searchActivities(
        @RequestParam(required = false) query: String?,
        @RequestParam(required = false) category: Category?,
        @RequestParam(required = false) maxPrice: Double?,
        @RequestParam(required = false) maxDuration: Int?
    ): List<Activity> = activityService.searchActivities(query, category, maxPrice, maxDuration)
}
