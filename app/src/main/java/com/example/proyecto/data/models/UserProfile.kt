package com.example.proyecto.data.models

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class UserProfile(
    val email: String,
    val username: String,
    val streak: Int,
    @SerializedName("lastActivity")
    private val lastActivityStr: String,
    @SerializedName("createdAt")
    private val createdAtStr: String
) {
    val lastActivity: LocalDateTime
        get() = LocalDateTime.parse(lastActivityStr, DateTimeFormatter.ISO_DATE_TIME)
    
    val createdAt: LocalDateTime
        get() = LocalDateTime.parse(createdAtStr, DateTimeFormatter.ISO_DATE_TIME)
} 