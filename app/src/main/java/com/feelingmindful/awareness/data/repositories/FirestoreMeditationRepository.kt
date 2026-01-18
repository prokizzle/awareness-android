package com.feelingmindful.awareness.data.repositories

import com.feelingmindful.awareness.domain.entities.Course
import com.feelingmindful.awareness.domain.entities.Meditation
import com.feelingmindful.awareness.domain.entities.ReflectionPrompt
import com.feelingmindful.awareness.domain.repositories.MeditationRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreMeditationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : MeditationRepository {

    override suspend fun getMeditation(id: String): Meditation {
        val doc = firestore
            .collection("meditations")
            .document(id)
            .get()
            .await()

        return doc.toMeditation()
    }

    override suspend fun getMeditations(ids: List<String>): List<Meditation> {
        if (ids.isEmpty()) return emptyList()

        return firestore
            .collection("meditations")
            .whereIn("__name__", ids.map { firestore.collection("meditations").document(it) })
            .get()
            .await()
            .documents
            .map { it.toMeditation() }
    }

    override suspend fun getCourse(id: String): Course {
        val doc = firestore
            .collection("courses")
            .document(id)
            .get()
            .await()

        return doc.toCourse()
    }

    override suspend fun getIntroCourse(): Course? {
        val docs = firestore
            .collection("courses")
            .whereEqualTo("isIntro", true)
            .limit(1)
            .get()
            .await()

        return docs.documents.firstOrNull()?.toCourse()
    }

    override suspend fun getUnplayedMeditations(excludeIds: List<String>, limit: Int): List<Meditation> {
        val query = firestore
            .collection("meditations")
            .whereEqualTo("courseId", null)
            .limit(limit.toLong())

        return query.get().await().documents
            .map { it.toMeditation() }
            .filter { it.id !in excludeIds }
    }

    override suspend fun getSystemPrompts(themes: List<String>): List<ReflectionPrompt> {
        val docs = firestore
            .collection("systemPrompts")
            .orderBy("order")
            .limit(3)
            .get()
            .await()

        return docs.documents.mapIndexed { index, doc ->
            ReflectionPrompt(
                id = doc.id,
                question = doc.getString("question") ?: "",
                order = doc.getLong("order")?.toInt() ?: index
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun com.google.firebase.firestore.DocumentSnapshot.toMeditation(): Meditation {
        val prompts = (get("prompts") as? List<Map<String, Any>>)?.mapIndexed { index, map ->
            ReflectionPrompt(
                id = map["id"] as? String ?: "$id-prompt-$index",
                question = map["question"] as? String ?: "",
                order = (map["order"] as? Long)?.toInt() ?: index
            )
        } ?: emptyList()

        return Meditation(
            id = id,
            title = getString("title") ?: "",
            description = getString("description") ?: "",
            duration = getLong("duration")?.toInt() ?: 0,
            audioUrl = getString("audioUrl"),
            hasChunks = getBoolean("hasChunks") ?: false,
            prompts = prompts,
            courseId = getString("courseId"),
            orderInCourse = getLong("orderInCourse")?.toInt(),
            themes = (get("themes") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            teacherId = getString("teacherId"),
            publishedAt = getTimestamp("publishedAt")?.toKotlinxInstant()
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun com.google.firebase.firestore.DocumentSnapshot.toCourse(): Course {
        return Course(
            id = id,
            title = getString("title") ?: "",
            description = getString("description") ?: "",
            meditationIds = (get("meditationIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            publishedAt = getTimestamp("publishedAt")?.toKotlinxInstant(),
            isIntro = getBoolean("isIntro") ?: false
        )
    }

    private fun Timestamp.toKotlinxInstant(): Instant =
        Instant.fromEpochSeconds(seconds, nanoseconds)
}
