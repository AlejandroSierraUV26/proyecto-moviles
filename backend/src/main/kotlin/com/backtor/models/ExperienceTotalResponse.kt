package com.backtor.models

import kotlinx.serialization.Serializable

@Serializable
data class ExperienceTotalResponse(
    val success: Boolean,
    val message: String,
    val totalExperience: Int
)
