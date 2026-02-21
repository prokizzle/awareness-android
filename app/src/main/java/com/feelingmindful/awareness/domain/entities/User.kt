package com.feelingmindful.awareness.domain.entities

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val authProvider: AuthProvider
)

@Serializable
enum class AuthProvider {
    GOOGLE,
    APPLE,
    EMAIL
}
