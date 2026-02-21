package com.feelingmindful.awareness.domain.entities

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * A short audio clip that redirects attention during meditation playback.
 *
 * Rescues are 5–15 second clips inserted into silence gaps between chunks.
 * Density is driven by user skill level.
 */
@Serializable
data class Rescue(
    val id: String,
    val title: String,
    val audioUrl: String,
    val durationMs: Int,
    val tags: List<String> = emptyList(),
    val publishedAt: Instant? = null,
    val sourceMeditationId: String? = null
) {
    val durationText: String
        get() {
            val seconds = durationMs / 1000
            val minutes = seconds / 60
            val secs = seconds % 60
            return "${minutes}:${secs.toString().padStart(2, '0')}"
        }
}
