package com.feelingmindful.awareness.data.repositories

import com.feelingmindful.awareness.domain.entities.Mantra
import com.feelingmindful.awareness.domain.entities.WindowType
import com.feelingmindful.awareness.domain.repositories.MantraRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreMantraRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : MantraRepository {

    override suspend fun getMantra(id: String): Mantra? {
        val doc = firestore
            .collection("mantras")
            .document(id)
            .get()
            .await()

        return if (doc.exists()) doc.toMantra() else null
    }

    override suspend fun getRandomMantra(windowType: WindowType): Mantra? {
        val docs = firestore
            .collection("mantras")
            .whereEqualTo("windowType", windowType.name.lowercase())
            .whereEqualTo("active", true)
            .get()
            .await()

        return docs.documents.randomOrNull()?.toMantra()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toMantra(): Mantra {
        val windowTypeStr = getString("windowType") ?: "morning"
        return Mantra(
            id = id,
            text = getString("text") ?: "",
            windowType = WindowType.valueOf(windowTypeStr.uppercase()),
            active = getBoolean("active") ?: true,
            attribution = getString("attribution")
        )
    }
}
