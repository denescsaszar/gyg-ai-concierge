package com.gyg.concierge.controller

import com.gyg.concierge.model.Activity
import com.gyg.concierge.model.Category
import com.gyg.concierge.service.ActivityService
import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Duration

@RestController
@RequestMapping("/api/activities")
class ActivityController(private val activityService: ActivityService) {

    /**
     * The catalog is static data. Letting browsers and any CDN in front of the
     * API cache it means most visits never reach this (sleeping) instance at all.
     */
    private val catalogCache: CacheControl = CacheControl
        .maxAge(Duration.ofMinutes(5))
        .cachePublic()
        .staleWhileRevalidate(Duration.ofDays(1))

    @GetMapping
    fun getAllActivities(): ResponseEntity<List<Activity>> =
        ResponseEntity.ok()
            .cacheControl(catalogCache)
            .body(activityService.getAllActivities())

    @GetMapping("/{id}")
    fun getActivityById(@PathVariable id: Long): ResponseEntity<Activity> {
        val activity = activityService.getActivityById(id)
        return if (activity != null) {
            ResponseEntity.ok().cacheControl(catalogCache).body(activity)
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
    ): ResponseEntity<List<Activity>> =
        ResponseEntity.ok()
            .cacheControl(catalogCache)
            .body(activityService.searchActivities(query, category, maxPrice, maxDuration))
}
