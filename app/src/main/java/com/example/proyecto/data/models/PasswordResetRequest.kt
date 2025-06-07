package com.example.proyecto.data.models

import kotlinx.serialization.Serializable


@Serializable
data class PasswordResetRequest(
    val email: String,
    val token: String,
    val newPassword: String
)
 // ForgotPasswordRequest.kt
 @Serializable
    data class ForgotPasswordRequest(
     val email: String
    )

