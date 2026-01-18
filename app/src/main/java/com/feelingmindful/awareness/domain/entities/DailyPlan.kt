package com.feelingmindful.awareness.domain.entities

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class DailyPlan(
    val id: String,
    val userId: String,
    val date: LocalDate,
    val tasks: DailyTasks,
    val mantraId: String? = null,
    val meditationId: String? = null,
    val allCompleted: Boolean = false,
    val completedAt: Instant? = null
) {
    companion object {
        fun create(userId: String, date: LocalDate, meditationId: String? = null): DailyPlan {
            return DailyPlan(
                id = date.toString(),
                userId = userId,
                date = date,
                tasks = DailyTasks.initial(),
                meditationId = meditationId
            )
        }
    }
}

@Serializable
data class DailyTasks(
    val meditation: TaskState,
    val reflection: TaskState,
    val cardio: TaskState,
    val meals: TaskState
) {
    companion object {
        fun initial(): DailyTasks = DailyTasks(
            meditation = TaskState(status = TaskStatus.AVAILABLE),
            reflection = TaskState(status = TaskStatus.LOCKED),
            cardio = TaskState(status = TaskStatus.LOCKED),
            meals = TaskState(status = TaskStatus.LOCKED)
        )
    }
}

@Serializable
data class TaskState(
    val status: TaskStatus,
    val completedAt: Instant? = null,
    val minutes: Int? = null,
    val autoCompleted: Boolean = false
)

@Serializable
enum class TaskStatus {
    LOCKED,
    AVAILABLE,
    IN_PROGRESS,
    COMPLETED;

    val isActionable: Boolean
        get() = this == AVAILABLE || this == IN_PROGRESS
}
