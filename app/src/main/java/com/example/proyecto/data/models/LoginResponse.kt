package com.example.proyecto.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class LoginResponse(
    @SerialName("token")
    val token: String
)

@Serializable
data class RegisterResponse(
    @SerialName("message")
    val message: String,
    @SerialName("token")
    val token: String
) 