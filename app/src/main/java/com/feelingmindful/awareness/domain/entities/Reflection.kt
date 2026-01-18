package com.feelingmindful.awareness.domain.entities

import kotlinx.serialization.Serializable

@Serializable
data class ReflectionPrompt(
    val id: String,
    val question: String,
    val order: Int
)

@Serializable
data class ReflectionAnswer(
    val promptId: String,
    val question: String,
    val answer: String
)
