package com.feelingmindful.awareness.domain.repositories

import com.feelingmindful.awareness.domain.entities.UserProgress

interface UserRepository {
    suspend fun getProgress(userId: String): UserProgress
    suspend fun updateProgress(userId: String, progress: UserProgress)
}
