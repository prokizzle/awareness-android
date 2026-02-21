package com.feelingmindful.awareness.domain.entities

import kotlinx.serialization.Serializable

@Serializable
data class UserProgress(
    val visitsCount: Int = 0,
    val streakDays: Int = 0,
    val completedMeditationIds: List<String> = emptyList(),
    val currentCourseId: String? = null,
    val currentCourseMeditationIndex: Int = 0,
    val skillLevel: SkillLevel = SkillLevel.BEGINNER
)
