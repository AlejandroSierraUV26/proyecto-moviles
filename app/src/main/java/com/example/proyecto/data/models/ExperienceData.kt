package com.example.proyecto.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ExperienceData(
    val date: String,
    val dayOfWeek: String,
    val experiencePoints: Int
)

@Serializable
data class ExperienceTotalResponse(
    val success: Boolean,
    val message: String,
    val totalExperience: Int
) 