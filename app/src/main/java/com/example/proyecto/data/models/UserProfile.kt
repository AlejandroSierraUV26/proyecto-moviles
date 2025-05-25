package com.example.proyecto.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class UserProfile(
    val email: String,
    val username: String,
    val streak: Int,
    @SerialName("lastActivity")
    private val lastActivityStr: String,
    @SerialName("createdAt")
    private val createdAtStr: String
) {
    val lastActivity: LocalDateTime
        get() = LocalDateTime.parse(lastActivityStr, DateTimeFormatter.ISO_DATE_TIME)
    
    val createdAt: LocalDateTime
        get() = LocalDateTime.parse(createdAtStr, DateTimeFormatter.ISO_DATE_TIME)
} 