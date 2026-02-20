package com.feelingmindful.awareness.data.repositories

import com.feelingmindful.awareness.domain.entities.DailyPlan
import com.feelingmindful.awareness.domain.entities.DailyTasks
import com.feelingmindful.awareness.domain.entities.FullReflection
import com.feelingmindful.awareness.domain.entities.MeditationSession
import com.feelingmindful.awareness.domain.entities.ReflectionAnswer
import com.feelingmindful.awareness.domain.entities.TaskState
import com.feelingmindful.awareness.domain.entities.TaskStatus
import com.feelingmindful.awareness.domain.repositories.DailyPlanRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreDailyPlanRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : DailyPlanRepository {

    override suspend fun getDailyPlan(userId: String, date: LocalDate): DailyPlan? {
        val doc = firestore
            .collection("users")
            .document(userId)
            .collection("dailyPlans")
            .document(date.toString())
            .get()
            .await()

        return if (doc.exists()) {
            doc.toDailyPlan(userId, date)
        } else {
            null
        }
    }

    override suspend fun saveDailyPlan(userId: String, plan: DailyPlan) {
        firestore
            .collection("users")
            .document(userId)
            .collection("dailyPlans")
            .document(plan.id)
            .set(plan.toMap())
            .await()
    }

    override suspend fun saveReflection(userId: String, date: LocalDate, answers: List<ReflectionAnswer>) {
        firestore
            .collection("users")
            .document(userId)
            .collection("dailyPlans")
            .document(date.toString())
            .collection("reflections")
            .document("answers")
            .set(
                mapOf(
                    "answers" to answers.map { answer ->
                        mapOf(
                            "promptId" to answer.promptId,
                            "question" to answer.question,
                            "answer" to answer.answer
                        )
                    },
                    "completedAt" to Timestamp.now()
                )
            )
            .await()
    }

    @Suppress("UNCHECKED_CAST")
    private fun com.google.firebase.firestore.DocumentSnapshot.toDailyPlan(
        userId: String,
        date: LocalDate
    ): DailyPlan {
        val tasks = get("tasks") as? Map<String, Any> ?: emptyMap()
        return DailyPlan(
            id = id,
            userId = userId,
            date = date,
            tasks = DailyTasks(
                meditation = tasks.parseTaskState("meditation"),
                reflection = tasks.parseTaskState("reflection"),
                cardio = tasks.parseTaskState("cardio"),
                meals = tasks.parseTaskState("meals")
            ),
            mantraId = getString("mantraId"),
            meditationId = getString("meditationId"),
            allCompleted = getBoolean("allCompleted") ?: false,
            completedAt = getTimestamp("completedAt")?.toKotlinxInstant()
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun Map<String, Any>.parseTaskState(key: String): TaskState {
        val task = get(key) as? Map<String, Any> ?: return TaskState(TaskStatus.LOCKED)
        return TaskState(
            status = TaskStatus.valueOf(task["status"] as? String ?: "LOCKED"),
            completedAt = (task["completedAt"] as? Timestamp)?.toKotlinxInstant(),
            minutes = (task["minutes"] as? Long)?.toInt(),
            autoCompleted = task["autoCompleted"] as? Boolean ?: false
        )
    }

    private fun DailyPlan.toMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "tasks" to mapOf(
            "meditation" to tasks.meditation.toMap(),
            "reflection" to tasks.reflection.toMap(),
            "cardio" to tasks.cardio.toMap(),
            "meals" to tasks.meals.toMap()
        ),
        "mantraId" to mantraId,
        "meditationId" to meditationId,
        "allCompleted" to allCompleted,
        "completedAt" to completedAt?.toTimestamp()
    )

    private fun TaskState.toMap(): Map<String, Any?> = mapOf(
        "status" to status.name,
        "completedAt" to completedAt?.toTimestamp(),
        "minutes" to minutes,
        "autoCompleted" to autoCompleted
    )

    private fun Timestamp.toKotlinxInstant(): Instant =
        Instant.fromEpochSeconds(seconds, nanoseconds)

    private fun Instant.toTimestamp(): Timestamp =
        Timestamp(epochSeconds, nanosecondsOfSecond)

    override suspend fun getSessions(userId: String, limit: Int): List<MeditationSession> {
        // Stub: session recording not yet wired; Library degrades gracefully
        return emptyList()
    }

    override suspend fun getReflections(userId: String, limit: Int): List<FullReflection> {
        // Stub: reflection history not yet wired; Library degrades gracefully
        return emptyList()
    }
}
