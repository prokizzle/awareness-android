package com.feelingmindful.awareness.domain.services

interface NotificationService {
    suspend fun hasPermission(): Boolean
    suspend fun requestPermission(): Boolean
    suspend fun scheduleDailyReminder(hour: Int, minute: Int)
    suspend fun cancelAllReminders()
}
