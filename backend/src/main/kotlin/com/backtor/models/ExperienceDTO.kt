package com.backtor.models
import kotlinx.serialization.Serializable

@Serializable
data class ExperienceDTO(
    val date: String,
    val dayOfWeek: String,
    val experiencePoints: Int
)
