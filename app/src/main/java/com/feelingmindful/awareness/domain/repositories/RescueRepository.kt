package com.feelingmindful.awareness.domain.repositories

import com.feelingmindful.awareness.domain.entities.Rescue

interface RescueRepository {
    suspend fun getRescues(): List<Rescue>
    suspend fun getRescue(id: String): Rescue
}
