package com.feelingmindful.awareness.domain.repositories

import com.feelingmindful.awareness.domain.entities.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun signInWithGoogle(idToken: String): User
    suspend fun signInWithApple(activityProvider: () -> Any): User
    suspend fun signOut()
    suspend fun deleteAccount()
}
