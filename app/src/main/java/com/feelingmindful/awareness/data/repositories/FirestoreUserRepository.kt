package com.feelingmindful.awareness.data.repositories

import com.feelingmindful.awareness.domain.entities.SkillLevel
import com.feelingmindful.awareness.domain.entities.UserProgress
import com.feelingmindful.awareness.domain.repositories.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreUserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    override suspend fun getProgress(userId: String): UserProgress {
        val doc = firestore
            .collection("users")
            .document(userId)
            .collection("data")
            .document("progress")
            .get()
            .await()

        return if (doc.exists()) {
            UserProgress(
                visitsCount = doc.getLong("visitsCount")?.toInt() ?: 0,
                streakDays = doc.getLong("streakDays")?.toInt() ?: 0,
                completedMeditationIds = (doc.get("completedMeditationIds") as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
                currentCourseId = doc.getString("currentCourseId"),
                currentCourseMeditationIndex = doc.getLong("currentCourseMeditationIndex")?.toInt() ?: 0,
                skillLevel = doc.getString("skillLevel")?.let {
                    try { SkillLevel.valueOf(it) } catch (_: IllegalArgumentException) { SkillLevel.BEGINNER }
                } ?: SkillLevel.BEGINNER
            )
        } else {
            UserProgress()
        }
    }

    override suspend fun updateProgress(userId: String, progress: UserProgress) {
        firestore
            .collection("users")
            .document(userId)
            .collection("data")
            .document("progress")
            .set(
                mapOf(
                    "visitsCount" to progress.visitsCount,
                    "streakDays" to progress.streakDays,
                    "completedMeditationIds" to progress.completedMeditationIds,
                    "currentCourseId" to progress.currentCourseId,
                    "currentCourseMeditationIndex" to progress.currentCourseMeditationIndex,
                    "skillLevel" to progress.skillLevel.name
                )
            )
            .await()
    }
}
