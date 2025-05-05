package com.backtor.models


import java.time.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual

@Serializable
data class UserProfile(
    val email: String,
    val username: String,
    val streak: Int,
    @Contextual val createdAt: LocalDateTime,
    @Contextual val lastActivity: LocalDateTime
)
