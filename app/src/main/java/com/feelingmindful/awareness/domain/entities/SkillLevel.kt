package com.feelingmindful.awareness.domain.entities

import kotlinx.serialization.Serializable

/**
 * User's meditation skill level, which determines rescue clip density during playback.
 *
 * Rescue density is the proportion of silence gaps that get a rescue clip inserted.
 */
@Serializable
enum class SkillLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED;

    val rescueDensity: Double
        get() = when (this) {
            BEGINNER -> 0.8
            INTERMEDIATE -> 0.4
            ADVANCED -> 0.1
        }
}
