package com.feelingmindful.awareness.data.repositories

import android.app.Activity
import com.feelingmindful.awareness.domain.entities.AuthProvider
import com.feelingmindful.awareness.domain.entities.User
import com.feelingmindful.awareness.domain.repositories.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val fbUser = firebaseAuth.currentUser
            val provider = when {
                fbUser == null -> AuthProvider.GOOGLE
                fbUser.providerData.any { it.providerId == "apple.com" } -> AuthProvider.APPLE
                else -> AuthProvider.GOOGLE
            }
            trySend(fbUser?.toUser(provider))
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithGoogle(idToken: String): User {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        return result.user?.toUser(AuthProvider.GOOGLE) ?: throw Exception("Sign in failed")
    }

    override suspend fun signInWithApple(activity: Activity): User {
        val provider = OAuthProvider.newBuilder("apple.com")
            .setScopes(listOf("email", "name"))
            .build()

        val result = auth.startActivityForSignInWithProvider(activity, provider).await()
        return result.user?.toUser(AuthProvider.APPLE) ?: throw Exception("Apple sign in failed")
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun deleteAccount() {
        auth.currentUser?.delete()?.await()
    }

    private fun com.google.firebase.auth.FirebaseUser.toUser(
        provider: AuthProvider = AuthProvider.GOOGLE
    ): User {
        return User(
            id = uid,
            email = email ?: "",
            displayName = displayName,
            photoUrl = photoUrl?.toString(),
            authProvider = provider
        )
    }
}
