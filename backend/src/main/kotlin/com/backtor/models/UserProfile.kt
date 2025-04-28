package com.backtor.models

import java.time.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val email: String,
    val username: String,
    val description: String,
    val createdAt: String,
    val updatedAt: String,
)
