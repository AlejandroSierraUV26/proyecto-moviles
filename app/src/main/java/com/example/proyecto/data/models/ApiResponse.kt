package com.example.proyecto.data.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ApiResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String
)
data class GoogleLoginRequest(
    @SerializedName("idToken")
    val idToken: String
)
data class GoogleLoginResponse(
    val token: String,
    val email: String,
    val username: String? = null
)
