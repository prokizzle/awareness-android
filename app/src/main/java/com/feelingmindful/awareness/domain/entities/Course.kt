package com.feelingmindful.awareness.domain.entities

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Course(
    val id: String,
    val title: String,
    val description: String,
    val meditationIds: List<String>,
    val publishedAt: Instant? = null,
    val isIntro: Boolean = false
)
