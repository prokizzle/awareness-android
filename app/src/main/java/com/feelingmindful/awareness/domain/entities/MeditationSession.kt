package com.feelingmindful.awareness.domain.entities

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class MeditationSession(
    val id: String,
    val meditationId: String,
    val meditationTitle: String,
    val duration: Int,
    val completedAt: Instant,
    val source: String
)
