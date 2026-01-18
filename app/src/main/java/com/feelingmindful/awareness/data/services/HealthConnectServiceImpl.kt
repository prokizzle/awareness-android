package com.feelingmindful.awareness.data.services

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.feelingmindful.awareness.domain.services.HealthConnectService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.todayIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : HealthConnectService {

    private val healthConnectClient: HealthConnectClient? by lazy {
        if (HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }
    }

    private val permissions = setOf(
        HealthPermission.getReadPermission(ExerciseSessionRecord::class)
    )

    override val isAvailable: Boolean
        get() = HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    override suspend fun hasPermissions(): Boolean {
        val client = healthConnectClient ?: return false
        val granted = client.permissionController.getGrantedPermissions()
        return permissions.all { it in granted }
    }

    override suspend fun requestPermissions(): Boolean {
        // Permission request must be handled by Activity
        // This returns current state - actual request happens via Activity result
        return hasPermissions()
    }

    override suspend fun getTodayWorkoutMinutes(): Int {
        val client = healthConnectClient ?: return 0

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val startOfDay = today.atStartOfDayIn(TimeZone.currentSystemDefault())
        val now = Clock.System.now()

        val request = ReadRecordsRequest(
            recordType = ExerciseSessionRecord::class,
            timeRangeFilter = TimeRangeFilter.between(
                startOfDay.toJavaInstant(),
                now.toJavaInstant()
            )
        )

        return try {
            val response = client.readRecords(request)
            response.records.sumOf { record ->
                val duration = java.time.Duration.between(record.startTime, record.endTime)
                duration.toMinutes().toInt()
            }
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun saveMindfulnessSession(durationSeconds: Int) {
        // MindfulnessSessionRecord is not available in the current Health Connect alpha
        // This will be implemented when the API becomes stable
    }
}
