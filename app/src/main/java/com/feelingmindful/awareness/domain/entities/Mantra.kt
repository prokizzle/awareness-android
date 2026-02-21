package com.feelingmindful.awareness.domain.entities

import kotlinx.serialization.Serializable

@Serializable
data class Mantra(
    val id: String,
    val text: String,
    val windowType: WindowType,
    val active: Boolean = true,
    val attribution: String? = null
)

@Serializable
enum class WindowType {
    MORNING,
    AFTERNOON,
    EVENING;

    companion object {
        fun fromHour(hour: Int): WindowType = when (hour) {
            in 5..11 -> MORNING
            in 12..16 -> AFTERNOON
            else -> EVENING
        }
    }
}
