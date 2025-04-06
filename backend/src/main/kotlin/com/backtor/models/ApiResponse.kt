package com.backtor.models

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse(
    val success: Boolean,
    val message: String
)
