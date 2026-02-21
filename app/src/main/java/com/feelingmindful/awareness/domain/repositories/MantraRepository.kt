package com.feelingmindful.awareness.domain.repositories

import com.feelingmindful.awareness.domain.entities.Mantra
import com.feelingmindful.awareness.domain.entities.WindowType

interface MantraRepository {
    suspend fun getMantra(id: String): Mantra?
    suspend fun getRandomMantra(windowType: WindowType): Mantra?
}
