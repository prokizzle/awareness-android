package com.feelingmindful.awareness.domain.services

interface HealthConnectService {
    val isAvailable: Boolean
    suspend fun hasPermissions(): Boolean
    suspend fun requestPermissions(): Boolean
    suspend fun getTodayWorkoutMinutes(): Int
    suspend fun saveMindfulnessSession(durationSeconds: Int)
}
