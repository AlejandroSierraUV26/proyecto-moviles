package com.example.proyecto.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ApiResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String
) 