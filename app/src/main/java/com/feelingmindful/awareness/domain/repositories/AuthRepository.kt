package com.feelingmindful.awareness.domain.repositories

import android.app.Activity
import com.feelingmindful.awareness.domain.entities.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun signInWithGoogle(idToken: String): User
    suspend fun signInWithApple(activity: Activity): User
    suspend fun signOut()
    suspend fun deleteAccount()
}
