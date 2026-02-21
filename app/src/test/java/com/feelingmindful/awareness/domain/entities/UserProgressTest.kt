package com.feelingmindful.awareness.domain.entities

import org.junit.Assert.assertEquals
import org.junit.Test

class UserProgressTest {

    @Test
    fun `default skill level is BEGINNER`() {
        val progress = UserProgress()
        assertEquals(SkillLevel.BEGINNER, progress.skillLevel)
    }

    @Test
    fun `skill level can be set to any value`() {
        val progress = UserProgress(skillLevel = SkillLevel.ADVANCED)
        assertEquals(SkillLevel.ADVANCED, progress.skillLevel)
    }

    @Test
    fun `copy preserves skill level`() {
        val original = UserProgress(
            visitsCount = 10,
            streakDays = 5,
            skillLevel = SkillLevel.INTERMEDIATE
        )
        val copy = original.copy(visitsCount = 11)

        assertEquals(SkillLevel.INTERMEDIATE, copy.skillLevel)
        assertEquals(11, copy.visitsCount)
        assertEquals(5, copy.streakDays)
    }

    @Test
    fun `default values are backward compatible`() {
        val progress = UserProgress()

        assertEquals(0, progress.visitsCount)
        assertEquals(0, progress.streakDays)
        assertEquals(emptyList<String>(), progress.completedMeditationIds)
        assertEquals(null, progress.currentCourseId)
        assertEquals(0, progress.currentCourseMeditationIndex)
        assertEquals(SkillLevel.BEGINNER, progress.skillLevel)
    }
}
