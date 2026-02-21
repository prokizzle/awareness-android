package com.feelingmindful.awareness.domain.entities

import org.junit.Assert.assertEquals
import org.junit.Test

class SkillLevelTest {

    @Test
    fun `beginner has 80 percent rescue density`() {
        assertEquals(0.8, SkillLevel.BEGINNER.rescueDensity, 0.001)
    }

    @Test
    fun `intermediate has 40 percent rescue density`() {
        assertEquals(0.4, SkillLevel.INTERMEDIATE.rescueDensity, 0.001)
    }

    @Test
    fun `advanced has 10 percent rescue density`() {
        assertEquals(0.1, SkillLevel.ADVANCED.rescueDensity, 0.001)
    }

    @Test
    fun `rescue density decreases with skill level`() {
        val densities = SkillLevel.entries.map { it.rescueDensity }
        assertEquals(densities, densities.sortedDescending())
    }

    @Test
    fun `valueOf round-trips correctly`() {
        for (level in SkillLevel.entries) {
            assertEquals(level, SkillLevel.valueOf(level.name))
        }
    }
}
