package com.backtor.models


import java.time.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual

@Serializable
data class UserProfile(
    val email: String,
    val username: String,
    val streak: Int,
    val experienceScore: Int, // experiencia de los últimos 7 días
    val experienceTotal: Int,
    @Serializable(with = LocalDateTimeSerializer::class) val lastActivity: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) val createdAt: LocalDateTime,
    val profileImageUrl: String? = null
)