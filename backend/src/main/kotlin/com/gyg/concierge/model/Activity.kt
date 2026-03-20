package com.gyg.concierge.model

data class Activity(
    val id: Long,
    val title: String,
    val description: String,
    val city: String,
    val category: Category,
    val priceEur: Double,
    val durationMinutes: Int,
    val rating: Double,
    val reviewCount: Int,
    val imageUrl: String,
    val highlights: List<String>,
    val isBookable: Boolean = true
)

enum class Category {
    TOURS,
    FOOD_AND_DRINK,
    MUSEUMS,
    OUTDOOR,
    WORKSHOPS,
    NIGHTLIFE,
    FAMILY,
    ADVENTURE
}
