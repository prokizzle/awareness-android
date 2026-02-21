package com.feelingmindful.awareness.domain.entities

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class FullReflection(
    val id: String,
    val date: LocalDate,
    val meditationId: String,
    val meditationTitle: String,
    val answers: List<ReflectionAnswer>,
    val createdAt: Instant
)
