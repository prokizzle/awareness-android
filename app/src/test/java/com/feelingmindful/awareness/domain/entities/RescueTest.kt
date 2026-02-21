package com.feelingmindful.awareness.domain.entities

import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RescueTest {

    @Test
    fun `durationText formats correctly for short clips`() {
        val rescue = createRescue(durationMs = 8000)
        assertEquals("0:08", rescue.durationText)
    }

    @Test
    fun `durationText formats correctly for clips over a minute`() {
        val rescue = createRescue(durationMs = 75000)
        assertEquals("1:15", rescue.durationText)
    }

    @Test
    fun `default values are applied`() {
        val rescue = Rescue(
            id = "r1",
            title = "Return to breath",
            audioUrl = "https://example.com/audio.mp3",
            durationMs = 5000
        )

        assertEquals(emptyList<String>(), rescue.tags)
        assertNull(rescue.publishedAt)
        assertNull(rescue.sourceMeditationId)
    }

    @Test
    fun `all fields are preserved`() {
        val now = Instant.fromEpochSeconds(1700000000)
        val rescue = Rescue(
            id = "r1",
            title = "Gently return",
            audioUrl = "https://storage.example.com/rescues/r1.mp3",
            durationMs = 12000,
            tags = listOf("breath", "attention"),
            publishedAt = now,
            sourceMeditationId = "med-123"
        )

        assertEquals("r1", rescue.id)
        assertEquals("Gently return", rescue.title)
        assertEquals(12000, rescue.durationMs)
        assertEquals(listOf("breath", "attention"), rescue.tags)
        assertEquals(now, rescue.publishedAt)
        assertEquals("med-123", rescue.sourceMeditationId)
    }

    @Test
    fun `data class equality works`() {
        val a = createRescue(id = "r1", durationMs = 5000)
        val b = createRescue(id = "r1", durationMs = 5000)
        assertEquals(a, b)
    }

    private fun createRescue(
        id: String = "test-rescue",
        durationMs: Int = 5000
    ) = Rescue(
        id = id,
        title = "Test Rescue",
        audioUrl = "https://example.com/audio.mp3",
        durationMs = durationMs
    )
}
