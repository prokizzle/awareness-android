package com.feelingmindful.awareness.data.repositories

import com.feelingmindful.awareness.data.toKotlinxInstant
import com.feelingmindful.awareness.domain.entities.Rescue
import com.feelingmindful.awareness.domain.repositories.RescueRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRescueRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : RescueRepository {

    override suspend fun getRescues(): List<Rescue> {
        return firestore
            .collection("rescues")
            .orderBy("publishedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
            .map { it.toRescue() }
    }

    override suspend fun getRescue(id: String): Rescue {
        val doc = firestore
            .collection("rescues")
            .document(id)
            .get()
            .await()

        if (!doc.exists()) {
            throw NoSuchElementException("Rescue not found: $id")
        }

        return doc.toRescue()
    }

    @Suppress("UNCHECKED_CAST")
    private fun com.google.firebase.firestore.DocumentSnapshot.toRescue(): Rescue {
        return Rescue(
            id = id,
            title = getString("title") ?: "",
            audioUrl = getString("audioUrl") ?: "",
            durationMs = getLong("durationMs")?.toInt() ?: 0,
            tags = (get("tags") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            publishedAt = getTimestamp("publishedAt")?.toKotlinxInstant(),
            sourceMeditationId = getString("sourceMeditationId")
        )
    }

}
