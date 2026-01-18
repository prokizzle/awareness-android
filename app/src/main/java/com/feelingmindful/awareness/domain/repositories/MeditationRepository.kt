package com.feelingmindful.awareness.domain.repositories

import com.feelingmindful.awareness.domain.entities.Course
import com.feelingmindful.awareness.domain.entities.Meditation
import com.feelingmindful.awareness.domain.entities.ReflectionPrompt

interface MeditationRepository {
    suspend fun getMeditation(id: String): Meditation
    suspend fun getMeditations(ids: List<String>): List<Meditation>
    suspend fun getCourse(id: String): Course
    suspend fun getIntroCourse(): Course?
    suspend fun getUnplayedMeditations(excludeIds: List<String>, limit: Int): List<Meditation>
    suspend fun getSystemPrompts(themes: List<String>): List<ReflectionPrompt>
}
