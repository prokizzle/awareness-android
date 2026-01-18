package com.feelingmindful.awareness.domain.entities

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Meditation(
    val id: String,
    val title: String,
    val description: String,
    val duration: Int,
    val audioUrl: String? = null,
    val hasChunks: Boolean = false,
    val prompts: List<ReflectionPrompt> = emptyList(),
    val courseId: String? = null,
    val orderInCourse: Int? = null,
    val themes: List<String> = emptyList(),
    val teacherId: String? = null,
    val publishedAt: Instant? = null
) {
    val durationText: String
        get() {
            val minutes = duration / 60
            return "$minutes min"
        }
}
